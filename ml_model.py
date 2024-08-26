import pandas as pd
import numpy as np
import scipy
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Dropout
from tensorflow.keras.callbacks import EarlyStopping
from sklearn.metrics import classification_report, accuracy_score, confusion_matrix
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.model_selection import train_test_split

# Load the data into pandas DataFrames
# Replace the following paths with the actual paths to your data files
data_df = pd.read_csv('all_first_jobs.csv')
results_df = pd.read_csv('all_jobs_first_jobs.csv')

# Merging the data and results tables based on 'jobreference' and 'equipment'
merged_df = pd.merge(data_df, results_df, on=['jobreference', 'equipment'], how='left')

# Convert 'timestamp' to datetime
merged_df['timestamp'] = pd.to_datetime(merged_df['timestamp'])

# Calculate elapsed time in seconds
reference_time = merged_df['timestamp'].min()
merged_df['elapsed_time_seconds'] = (merged_df['timestamp'] - reference_time).dt.total_seconds()

# Drop columns not needed for modeling
X = merged_df.drop(columns=['id_x', 'id_y', 'jobreference', 'inserttime_x', 'inserttime_y', 'eieventjobid', 'timestamp'])

print(X.head(100))

# Create a binary label indicating the presence of any anomaly
merged_df['anomaly'] = merged_df[['eventdeletion', 'eventduplication', 
                                  'jobruntimeanomaly', 'waferruntimeanomaly']].sum(axis=1)
merged_df['anomaly'] = np.where(merged_df['anomaly'] > 0, 1, 0)

# Target variable
y = merged_df['anomaly']

# Splitting the data into training and test sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Convert y_train to float32
y_train = y_train.astype('float32')
y_test = y_test.astype('float32')

# Define categorical and numerical columns
categorical_cols = ['equipment', 'equipmenttoolrecipe', 'equipmentstate', 'equipmenttype', 
                    'equipmentgroup', 'eieventtype', 'area']
numerical_cols = X_train.columns.difference(categorical_cols)  # Including 'elapsed_time_seconds'

# Create transformers for categorical and numerical data
preprocessor = ColumnTransformer(
    transformers=[
        ('num', StandardScaler(), numerical_cols),
        ('cat', OneHotEncoder(handle_unknown='ignore'), categorical_cols)
    ])

# Fit the preprocessor and transform the data
X_train_processed = preprocessor.fit_transform(X_train)
X_test_processed = preprocessor.transform(X_test)

# Convert sparse matrices to dense arrays if necessary
if isinstance(X_train_processed, scipy.sparse.spmatrix):
    X_train_processed = X_train_processed.toarray()
if isinstance(X_test_processed, scipy.sparse.spmatrix):
    X_test_processed = X_test_processed.toarray()

# Ensure the data is in float32 format
X_train_processed = X_train_processed.astype('float32')
X_test_processed = X_test_processed.astype('float32')

# Convert processed data to TensorFlow tensors
X_train_processed_tensor = tf.convert_to_tensor(X_train_processed, dtype=tf.float32)
X_test_processed_tensor = tf.convert_to_tensor(X_test_processed, dtype=tf.float32)

# Build the TensorFlow model
model = Sequential([
    Dense(64, activation='relu', input_shape=(X_train_processed.shape[1],)),
    Dense(32, activation='relu'),
    Dense(16, activation='relu'),
    Dense(1, activation='sigmoid')  # Assuming binary classification
])

model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

# Early stopping to prevent overfitting
early_stopping = EarlyStopping(monitor='val_loss', patience=10, restore_best_weights=True)

# Train the model
history = model.fit(X_train_processed_tensor, y_train, validation_split=0.2, epochs=10, batch_size=32, 
                    callbacks=[early_stopping], verbose=1)

# Predictions
y_pred = (model.predict(X_test_processed_tensor) > 0.5).astype("int32")

# Evaluation
print("Accuracy:", accuracy_score(y_test, y_pred))
print("Confusion Matrix:\n", confusion_matrix(y_test, y_pred))
print("Classification Report:\n", classification_report(y_test, y_pred))

# Save the model (optional)
model.save('anomaly_detection_model.h5')

# Plotting training & validation loss values
import matplotlib.pyplot as plt

plt.plot(history.history['loss'])
plt.plot(history.history['val_loss'])
plt.title('Model loss')
plt.ylabel('Loss')
plt.xlabel('Epoch')
plt.legend(['Train', 'Validation'], loc='upper right')
plt.show()
