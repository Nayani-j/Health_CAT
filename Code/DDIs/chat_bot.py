import pandas as pd
import streamlit as st
import google.generativeai as genai
from langchain_google_genai import ChatGoogleGenerativeAI

model = ChatGoogleGenerativeAI(model="gemini-pro", temperature=0.2, google_api_key="Enter your API-key")

def loading_data(file_path):
    try:
        df = pd.read_csv(file_path, encoding='utf-8')
        df.columns = df.columns.str.strip()
        print("Data loaded successfully.")
        print("Columns in the dataset:", df.columns.tolist())
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
def finding_interactions(dataframe, drug1_name, drug2_name):
    result = dataframe[((dataframe['Drug_A'].str.lower() == drug1_name.lower()) &
                        (dataframe['Drug_B'].str.lower() == drug2_name.lower())) |
                       ((dataframe['Drug_A'].str.lower() == drug2_name.lower()) &
                        (dataframe['Drug_B'].str.lower() == drug1_name.lower()))]
    if not result.empty:
        return result['Level'].values[0]
    else:
        return "No interaction found between these drugs."
def chat_with_gemini(drug_a, drug_b):
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
    interaction_data_path = '/Users/snehajadhav/PycharmProjects/pythonProject/HackX/ddinter_downloads_code_H.csv'
    interaction_df = loading_data(interaction_data_path)
    drug_name1 = st.text_input("Enter the first drug name:")
    drug_name2 = st.text_input("Enter the second drug name:")
    if drug_name1 and drug_name2:
        if interaction_df is not None:
            interaction_level = finding_interactions(interaction_df, drug_name1, drug_name2)
            st.write(f"Interaction Level: {interaction_level}")
            interaction_details = chat_with_gemini(drug_name1, drug_name2)
            st.write(interaction_details)
        else:
            st.write("Interaction data could not be loaded.")
    else:
        st.write("Please enter both drug names to check for interactions.")

if __name__ == "__main__":
    main()
