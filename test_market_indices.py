#!/usr/bin/env python3
"""
Test script to verify the market indices API endpoint
"""

import requests
import json
import time
from datetime import datetime

def test_market_indices_api():
    """Test the market indices API endpoint"""
    
    base_url = "http://localhost:8082"
    api_endpoint = f"{base_url}/api/indices"
    
    print("🧪 Testing Market Indices API")
    print("=" * 50)
    print(f"API Endpoint: {api_endpoint}")
    print(f"Test Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print()
    
    try:
        # Test the API endpoint
        print("📡 Making API request...")
        response = requests.get(api_endpoint, timeout=10)
        
        print(f"Status Code: {response.status_code}")
        print(f"Response Headers: {dict(response.headers)}")
        print()
        
        if response.status_code == 200:
            data = response.json()
            print("✅ API Response:")
            print(json.dumps(data, indent=2))
            print()
            
            # Validate response structure
            required_fields = ['nifty', 'banknifty', 'marketOpen', 'lastUpdated']
            missing_fields = [field for field in required_fields if field not in data]
            
            if missing_fields:
                print(f"❌ Missing required fields: {missing_fields}")
                return False
            else:
                print("✅ All required fields present")
            
            # Validate data types
            if not isinstance(data['nifty'], (int, float)):
                print("❌ Nifty price should be a number")
                return False
                
            if not isinstance(data['banknifty'], (int, float)):
                print("❌ Bank Nifty price should be a number")
                return False
                
            if not isinstance(data['marketOpen'], bool):
                print("❌ Market open status should be a boolean")
                return False
                
            print("✅ All data types are correct")
            
            # Check if prices are reasonable
            if data['nifty'] < 10000 or data['nifty'] > 50000:
                print(f"⚠️  Nifty price ({data['nifty']}) seems unusual")
            else:
                print(f"✅ Nifty price ({data['nifty']}) looks reasonable")
                
            if data['banknifty'] < 20000 or data['banknifty'] > 100000:
                print(f"⚠️  Bank Nifty price ({data['banknifty']}) seems unusual")
            else:
                print(f"✅ Bank Nifty price ({data['banknifty']}) looks reasonable")
            
            print()
            print("🎉 API test completed successfully!")
            return True
            
        else:
            print(f"❌ API request failed with status {response.status_code}")
            print(f"Response: {response.text}")
            return False
            
    except requests.exceptions.ConnectionError:
        print("❌ Connection failed - is the backend server running?")
        print("   Make sure to start the Spring Boot application on port 8082")
        return False
        
    except requests.exceptions.Timeout:
        print("❌ Request timed out")
        return False
        
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False

def test_market_status_api():
    """Test the market status API endpoint"""
    
    base_url = "http://localhost:8082"
    api_endpoint = f"{base_url}/api/indices/market-status"
    
    print("\n🧪 Testing Market Status API")
    print("=" * 50)
    print(f"API Endpoint: {api_endpoint}")
    print()
    
    try:
        response = requests.get(api_endpoint, timeout=10)
        
        if response.status_code == 200:
            is_open = response.json()
            print(f"✅ Market Status: {'OPEN' if is_open else 'CLOSED'}")
            return True
        else:
            print(f"❌ Market status API failed with status {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Market status API error: {e}")
        return False

if __name__ == "__main__":
    print("🚀 Starting Market Indices API Tests")
    print("=" * 60)
    
    # Test market indices API
    indices_success = test_market_indices_api()
    
    # Test market status API
    status_success = test_market_status_api()
    
    print("\n" + "=" * 60)
    print("📊 Test Results Summary:")
    print(f"Market Indices API: {'✅ PASS' if indices_success else '❌ FAIL'}")
    print(f"Market Status API: {'✅ PASS' if status_success else '❌ FAIL'}")
    
    if indices_success and status_success:
        print("\n🎉 All tests passed! The API is working correctly.")
        print("\n💡 Next steps:")
        print("   1. Start the frontend application")
        print("   2. Verify that Nifty and Bank Nifty prices are displayed")
        print("   3. Check that market status shows correctly")
    else:
        print("\n❌ Some tests failed. Please check the backend server.")
        print("\n🔧 Troubleshooting:")
        print("   1. Make sure the Spring Boot application is running")
        print("   2. Check that the port 8082 is not blocked")
        print("   3. Verify the API endpoints are accessible")
