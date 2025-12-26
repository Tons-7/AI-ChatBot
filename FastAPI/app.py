from transformers import AutoTokenizer, AutoModelForCausalLM
import torch

model_name = "microsoft/Phi-3-mini-4k-instruct"

tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForCausalLM.from_pretrained(
    model_name,
    dtype=torch.float16,
    device_map='cuda',
    trust_remote_code=True
)
print("Model loaded")

def chat(messages):
    prompt = tokenizer.apply_chat_template(
        messages,
        tokenize=False,
        add_generation_prompt=True
    )

    inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

    with torch.no_grad():
        output_ids = model.generate(
            **inputs,
            max_new_tokens=400,
            do_sample=True,
            temperature=0.1,
            top_p=0.4,
            top_k=50,
            eos_token_id=tokenizer.eos_token_id,
            pad_token_id=tokenizer.pad_token_id,
            use_cache=False
        )

    response_text = tokenizer.decode(
        output_ids[0][inputs['input_ids'].shape[1]:],
        skip_special_tokens=True
    ).strip()

    print("AI:", response_text)
    return response_text

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class ChatMessage(BaseModel):
    role: str
    content: str

class ChatRequest(BaseModel):
    conversation_id: int
    history: list[ChatMessage]
    user_input: str

@app.post("/api/ai_response")
def ai_response(request: ChatRequest):

    messages = [{"role": "system", "content": "You are a helpful assistant."}]
    messages += [msg.dict() for msg in request.history]
    messages.append({"role": "user", "content": request.user_input})

    reply = chat(messages)
    return {"content": reply}

from pyngrok import ngrok
import uvicorn
import asyncio

# Exposed to public url because of google colab usage - Change as needed
# You must first get an auth token from ngrok if you wish to do the same

if __name__ == "__main__":
    ngrok.kill()

    ngrok.set_auth_token("your_auth_token")

    public_url = ngrok.connect(8000)
    print("Public URL:", public_url.public_url)

    config = uvicorn.Config(app, host="0.0.0.0", port=8000)
    server = uvicorn.Server(config)
    asyncio.create_task(server.serve())