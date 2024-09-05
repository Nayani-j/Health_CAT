# python -m streamlit run EHR_DRUG_FINDER.py 
import streamlit as st
from PIL import Image
import pytesseract
import re
import os
from langchain.text_splitter import RecursiveCharacterTextSplitter
from dotenv import load_dotenv

# Load environment variables
load_dotenv()
api_key = os.getenv("GOOGLE_API_KEY")

# Helper functions
def get_image_text(image_files):
    texts = {}
    for image_file in image_files:
        image = Image.open(image_file)
        # Perform OCR on the image
        extracted_text = pytesseract.image_to_string(image)
        # Save the extracted text to a .txt file with the same name as the image
        file_name = os.path.splitext(image_file.name)[0] + ".txt"
        with open(file_name, "w") as file:
            file.write(extracted_text)
        texts[file_name] = extracted_text
    return texts

def read_text_file(file_path):
    with open(file_path, "r") as file:
        return file.read()

def get_text_chunks(text):
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=10000, chunk_overlap=1000)
    chunks = text_splitter.split_text(text)
    return chunks

def clean_text(text):
    # Remove unwanted symbols and characters
    cleaned_text = re.sub(r'[^\w\s]', '', text)  # Remove punctuation
    cleaned_text = re.sub(r'\s+', ' ', cleaned_text).strip()  # Remove extra whitespace
    return cleaned_text

def extract_summary_section(text):
    # Extract the "Summary of patient information" section
    pattern = rf'Summary of patient information\s*([\s\S]*?)(?:\n\n|\n|Immunizations|Additional Information About People & Organizations|\Z)'
    match = re.search(pattern, text, re.IGNORECASE)
    if match:
        return match.group(1).strip()
    return "No summary section found."

import re

def extract_text_between_occurrences(text, first_title, second_title):
    # Extract text between the second occurrence of first_title and the second occurrence of second_title
    first_matches = list(re.finditer(re.escape(first_title), text, re.IGNORECASE))
    second_matches = list(re.finditer(re.escape(second_title), text, re.IGNORECASE))
    
    if len(first_matches) < 2 or len(second_matches) < 2:
        return "Insufficient occurrences of titles in the text."
    
    start_pos = first_matches[1].end()  # End of the second occurrence of first_title
    end_pos = second_matches[1].start()  # Start of the second occurrence of second_title
    
    return text[start_pos:end_pos].strip()

def extract_medications_from_txt(txt_file):
    # Read the .txt file and extract medications between "Medications" and "Immunizations"
    file_content = read_text_file(txt_file)
    
    # Extract the section between the second occurrence of "Medications" and "Immunizations"
    relevant_text = extract_text_between_occurrences(file_content, "Medications", "Immunizations")
    
    # Updated regex to capture medication names only, excluding the trailing alphanumeric string
    medication_lines = re.findall(r'(\b[A-Za-z]+(?:\s+[A-Za-z]+)?)\s+Active', relevant_text, re.IGNORECASE)
    
    # Format the result for display
    formatted_medications = "\n".join(medication_lines)
    
    return formatted_medications


def main():
    st.set_page_config(page_title="Extract Summary Information")
    st.header("Extract Summary Information from Image Documents 🐭🎀🎻")

    summary_info = ""  # Initialize variable to store summary info
    medication_info = ""  # Initialize variable to store medication info

    st.title("Upload Your Image Files")
    image_files = st.file_uploader("Upload image files", accept_multiple_files=True, type=["png", "jpg", "jpeg"])

    if st.button("Submit & Process"):
        with st.spinner("Processing..."):
            texts = get_image_text(image_files)

            # Process each saved .txt file
            for txt_file in texts.keys():
                # Extract and display medication information from the .txt file
                medication_info = extract_medications_from_txt(txt_file)
                st.write( medication_info)

            st.success("Done")

if __name__ == "__main__":
    main()
