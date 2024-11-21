from langchain_community.utilities import DuckDuckGoSearchAPIWrapper
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnablePassthrough
from langchain_openai import ChatOpenAI
import os

os.environ["OPENAI_API_KEY"] = "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"

template = """Provide a better search query for \
web search engine to answer the given question, end \
the queries with ’**’. Use the same language. Formulate the answer as question. Question: \
{x} Answer:"""
rewrite_prompt = ChatPromptTemplate.from_template(template)

# Parser to remove the `**`

distracted_query = "assicurazione sci"


def _parse(text):
    return text.strip('"').strip("**")


rewriter = rewrite_prompt | ChatOpenAI(temperature=0) | StrOutputParser() | _parse

x = rewriter.invoke({"x": distracted_query})

print(x)