import pandas as pd
import numpy as np
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.utils import to_categorical
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Embedding, LSTM, Dense, Dropout
import joblib
import tensorflow as tf
from tensorflow.keras.models import Model
from tensorflow.keras import layers

# This function will load the data
def loading_data(file_path):
    try:
        df = pd.read_csv(file_path, encoding='utf-8')
        df.columns = df.columns.str.strip()  # This step will get rid of white spaces
        print("Data loaded successfully.")
        print("Columns in the dataset:", df.columns.tolist())  # Print column names for convinience
        return df
    except FileNotFoundError:
        print(f"Error: The file at {file_path} was not found.")
    except pd.errors.EmptyDataError:
        print("Error: The file is empty.")
    except pd.errors.ParserError:
        print("Error: The file could not be parsed.")
    except Exception as e:
        print(f"An error occurred: {e}")

# This function will preprocess the data
def preprocess_data(dataframe):
    # Combine Drug_A and Drug_B 
    combined_drugs = dataframe['Drug_A'] + " " + dataframe['Drug_B']

    # Proceed to Tokenize the combined drug names
    tokenizer = Tokenizer()
    tokenizer.fit_on_texts(combined_drugs)
    sequences = tokenizer.texts_to_sequences(combined_drugs)

    # Padding 
    max_seq_length = max([len(seq) for seq in sequences])
    padded_sequences = pad_sequences(sequences, maxlen=max_seq_length)

    # Encoding the labels (interaction levels)
    label_encoder = LabelEncoder()
    labels = label_encoder.fit_transform(dataframe['Level'])

    # Convert labels to categorical (for multi-class classification)
    categorical_labels = to_categorical(labels)

    return padded_sequences, categorical_labels, max_seq_length, len(tokenizer.word_index) + 1, tokenizer, label_encoder

# LSTM model
def build_lstm_model(vocab_size, max_seq_length, num_classes):
    model = Sequential()
    model.add(Embedding(input_dim=vocab_size, output_dim=128, input_length=max_seq_length))
    model.add(LSTM(64, return_sequences=False))
    model.add(Dropout(0.2))
    model.add(Dense(32, activation='relu'))
    model.add(Dense(num_classes, activation='softmax'))  # Using num_classes here

    model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
    return model

# Train the model
def train_model(model, X_train, y_train, X_val, y_val, epochs=10, batch_size=32):
    history = model.fit(X_train, y_train, epochs=epochs, batch_size=batch_size, validation_data=(X_val, y_val))
    
    # Print all the scores (accuracy and loss for training and validation sets)
    print(f"Training Accuracy: {history.history['accuracy'][-1]}")
    print(f"Validation Accuracy: {history.history['val_accuracy'][-1]}")
    print(f"Training Loss: {history.history['loss'][-1]}")
    print(f"Validation Loss: {history.history['val_loss'][-1]}")

# Function to save everything to a single pkl file
def save_all(model, tokenizer, label_encoder, file_path):
    model_path = 'temp_model.h5'
    model.save(model_path)

   
    with open(file_path, 'wb') as f:
        joblib.dump({
            'model_path': model_path,
            'tokenizer': tokenizer,
            'label_encoder': label_encoder
        }, f)

    print("All components saved successfully in a single pkl file.") # Save everything to a single pkl file


def main():
    # path for the interaction data
    interaction_data_path = '/content/ddinter_downloads_code_H.csv'

    interaction_df = loading_data(interaction_data_path)

    if interaction_df is not None:
        # Preprocess 
        X, y, max_seq_length, vocab_size, tokenizer, label_encoder = preprocess_data(interaction_df)

        num_classes = y.shape[1]

        # Spliting data 
        X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42)

        # Building the LSTM model
        model = build_lstm_model(vocab_size, max_seq_length, num_classes)

        # Training the model
        train_model(model, X_train, y_train, X_val, y_val, epochs=10, batch_size=32)

        # Saving everything to a single pkl file
        save_all(model, tokenizer, label_encoder, 'model_and_components.pkl')

if __name__ == "__main__":
    main()
