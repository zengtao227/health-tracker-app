import requests
import json
import sys
import certifi
import google.auth
from google.auth.transport.requests import Request

API_URL = "https://stitch.googleapis.com/mcp"

def get_authenticated_headers():
    credentials, _ = google.auth.default(scopes=['https://www.googleapis.com/auth/cloud-platform'])
    credentials = credentials.with_quota_project("gen-lang-client-0012133935")
    session = requests.Session()
    session.verify = certifi.where()
    request = Request(session=session)
    credentials.refresh(request)
    return {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {credentials.token}",
        "X-Goog-User-Project": "gen-lang-client-0012133935"
    }

def call_tool(tool_name, arguments):
    payload = {
        "jsonrpc": "2.0",
        "method": "tools/call",
        "params": {
            "name": tool_name,
            "arguments": arguments
        },
        "id": 1
    }
    
    try:
        headers = get_authenticated_headers()
        response = requests.post(API_URL, headers=headers, json=payload, timeout=180, verify=certifi.where())
        
        if response.status_code != 200:
            print(f"Error: {response.status_code} - {response.text}")
            return None
            
        result = response.json()
        if "error" in result:
            print(f"RPC Error: {result['error']}")
            return None
            
        return result["result"]
    except Exception as e:
        print(f"Exception: {e}")
        return None

def main():
    if len(sys.argv) < 2:
        print("Usage: python stitch_client.py <command> [args]")
        return

    command = sys.argv[1]
    
    if command == "create_project":
        title = sys.argv[2] if len(sys.argv) > 2 else "New Project"
        res = call_tool("create_project", {"title": title})
        print(json.dumps(res, indent=2))
        
    elif command == "generate_ui":
        project_id = sys.argv[2]
        prompt = sys.argv[3]
        res = call_tool("generate_screen_from_text", {
            "projectId": project_id,
            "prompt": prompt,
            "deviceType": "MOBILE",
            "modelId": "GEMINI_3_PRO"
        })
        print(json.dumps(res, indent=2))
        
    elif command == "list_projects":
        res = call_tool("list_projects", {})
        print(json.dumps(res, indent=2))

if __name__ == "__main__":
    main()
