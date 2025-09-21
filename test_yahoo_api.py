#!/usr/bin/env python3
"""
Test script to verify Yahoo Finance API for NIFTY and BANKNIFTY
"""

import requests
import json

def test_yahoo_finance_api():
    """Test Yahoo Finance API for Indian indices"""
    
    # Yahoo Finance symbols for Indian indices
    symbols = {
        "NIFTY": "^NSEI",
        "BANKNIFTY": "^NSEBANK"
    }
    
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    }
    
    for name, symbol in symbols.items():
        try:
            url = f"https://query1.finance.yahoo.com/v8/finance/chart/{symbol}"
            print(f"\nTesting {name} ({symbol})...")
            print(f"URL: {url}")
            
            response = requests.get(url, headers=headers, timeout=10)
            
            if response.status_code == 200:
                data = response.json()
                chart = data.get("chart", {})
                result = chart.get("result", [])
                
                if result:
                    meta = result[0].get("meta", {})
                    regular_price = meta.get("regularMarketPrice")
                    previous_close = meta.get("previousClose")
                    
                    print(f"✅ {name} - Regular Market Price: {regular_price}")
                    print(f"✅ {name} - Previous Close: {previous_close}")
                else:
                    print(f"❌ {name} - No result data")
            else:
                print(f"❌ {name} - HTTP {response.status_code}")
                
        except Exception as e:
            print(f"❌ {name} - Error: {e}")

if __name__ == "__main__":
    print("Testing Yahoo Finance API for Indian Indices...")
    test_yahoo_finance_api()
