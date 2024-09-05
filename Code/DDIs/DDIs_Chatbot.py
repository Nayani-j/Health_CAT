import pandas as pd
import streamlit as st
import google.generativeai as genai
from langchain_google_genai import ChatGoogleGenerativeAI

# Initialize the Google Generative AI model
model = ChatGoogleGenerativeAI(model="gemini-pro", temperature=0.2, google_api_key="AIzaSyDpb1Ula5KBefhPo3wWr5X00yGOTowJr0M")

def loading_data(file_path):
    """
    Load the CSV file into a DataFrame.

    Args:
    file_path (str): The path to the CSV file.

    Returns:
    pandas.DataFrame: A DataFrame containing the data.
    """
    try:
        df = pd.read_csv(file_path, encoding='utf-8')
        df.columns = df.columns.str.strip()  # Removing white spaces
        print("Data loaded successfully.")
        print("Columns in the dataset:", df.columns.tolist())  # Print column names
        return df
    except FileNotFoundError:
        print(f"Error: The file at {file_path} was not found.")
    except pd.errors.EmptyDataError:
        print("Error: The file is empty.")
    except pd.errors.ParserError:
        print("Error: The file could not be parsed.")
    except Exception as e:
        print(f"An error occurred: {e}")
        return None

def finding_drug_code(dataframe, drug_name):
    """
    Find the drug code corresponding to a drug name from the DataFrame.

    Args:
    dataframe (pandas.DataFrame): The DataFrame containing drug names and codes.
    drug_name (str): The drug name to look up.

    Returns:
    str: The drug code if found, else None.
    """
    result = dataframe[dataframe['Drug_Name'].str.lower() == drug_name.lower()]
    if not result.empty:
        return result['Code'].values[0]
    else:
        return None

def finding_interactions(dataframe, drug1, drug2):
    """
    Find interactions between two drugs from the DataFrame.

    Args:
    dataframe (pandas.DataFrame): The DataFrame containing drug interactions.
    drug1 (str): The first drug code.
    drug2 (str): The second drug code.

    Returns:
    str: The interaction description between the two drugs if found, else a message.
    """
    result = dataframe[((dataframe['# STITCH 1'] == drug1) & (dataframe['STITCH 2'] == drug2)) |
                       ((dataframe['# STITCH 1'] == drug2) & (dataframe['STITCH 2'] == drug1))]

    if not result.empty:
        return result['Side Effect Name'].values[0]
    else:
        return "No interaction found between these drugs."

def chat_with_gemini(drug_a, drug_b):
    """
    Use the Gemini model to provide detailed interaction information.

    Args:
    drug_a (str): The first drug name.
    drug_b (str): The second drug name.

    Returns:
    str: The response from the Gemini model.
    """
    prompt_template = """
    You are a healthcare assistant specializing in drug-drug interactions (DDI). 
    You will help in understanding the possible aftereffects when two drugs are taken together.
    Given that Drug A is {drug_a} and Drug B is {drug_b}, can you explain the potential aftereffects or side effects that might occur due to their interaction? 
    Please provide a detailed response considering the known interaction between these drugs.
    """
    prompt = prompt_template.format(drug_a=drug_a, drug_b=drug_b)
    response = model.predict(prompt)
    return response

def main():
    st.title("Health Cat Chatbot 🐱🎀")

    st.write("Welcome! Ask your healthcare-related questions below:")

    # Load interaction data and drug code data
    interaction_data_path = '/Users/snehajadhav/PycharmProjects/pythonProject/HackX/ChChSe-Decagon_polypharmacy.csv'
    drug_code_data_path = '/Users/snehajadhav/PycharmProjects/pythonProject/HackX/drug_names copy.csv'

    interaction_df = loading_data(interaction_data_path)
    drug_code_df = loading_data(drug_code_data_path)

    drug_name1 = st.text_input("Enter the first drug name:")
    drug_name2 = st.text_input("Enter the second drug name:")

    if drug_name1 and drug_name2:
        if interaction_df is not None and drug_code_df is not None:
            drug_code1 = finding_drug_code(drug_code_df, drug_name1)
            drug_code2 = finding_drug_code(drug_code_df, drug_name2)

            if drug_code1 and drug_code2:
                interaction = finding_interactions(interaction_df, drug_code1, drug_code2)
                st.write(f"Interaction: {interaction}")
                interaction_details = chat_with_gemini(drug_name1, drug_name2)
                st.write(interaction_details)
            else:
                st.write("Could not find one or both drug codes. Please check the drug names entered.")
        else:
            st.write("Data could not be loaded. Please check the file paths and try again.")
    else:
        st.write("Please enter both drug names to check for interactions.")

if __name__ == "__main__":
    main()
