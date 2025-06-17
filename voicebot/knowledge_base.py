import json
import boto3
import os

import Configs

KB_ID = Configs.CONFIG['sonic']['kb_id']
KB_REGION = 'us-east-1'
bedrock_agent_runtime = boto3.client('bedrock-agent-runtime', region_name=KB_REGION, aws_access_key_id=Configs.CONFIG['aws']['access_key_id'],aws_secret_access_key=Configs.CONFIG['aws']['secret_access_key'])


def retrieve_kb(query):
    results = []
    # Call KB
    response = bedrock_agent_runtime.retrieve(
        knowledgeBaseId=KB_ID,
        retrievalConfiguration={
            'vectorSearchConfiguration': {
                'numberOfResults': 1,
                'overrideSearchType': 'SEMANTIC',
            }
        },
        retrievalQuery={
            'text': query
        }
    )
    if "retrievalResults" in response:
        for r in response["retrievalResults"]:
            results.append(r["content"]["text"])
    return results


def retrieve_and_generation(query):
    results = []

    response = bedrock_agent_runtime.retrieve_and_generate(
        input={
            'text': query
        },
        retrieveAndGenerateConfiguration={
            'type': 'KNOWLEDGE_BASE',
            'knowledgeBaseConfiguration': {
                'knowledgeBaseId': KB_ID,
                'modelArn': 'anthropic.claude-3-haiku-20240307-v1:0',
                'retrievalConfiguration': {
                    'vectorSearchConfiguration': {
                        'numberOfResults': 2  # will fetch top N documents which closely match the query
                    }
                },
                'generationConfiguration': {
                    'promptTemplate': {
                        'textPromptTemplate': Configs.kb_prompt
                    }
                }
            }
        }
    )
    if "citations" in response:
        for r in response["citations"]:
            results.append(r["generatedResponsePart"]["textResponsePart"]["text"])
    return results