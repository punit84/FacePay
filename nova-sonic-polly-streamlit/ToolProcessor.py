import pytz
import random
import hashlib
import datetime
import uuid
import asyncio
import json
import requests

import knowledge_base

class ToolProcessor:

    def __init__(self):
        # ThreadPoolExecutor could be used for complex implementations
        self.tasks = {}

    def perform_curl(self, endpoint_url):
        try:
            response = requests.get(endpoint_url)
            print(f"Status Code: {response.status_code}")
            print("Response Body:")
            print(response.text)
            #{"company":"amazon","symbol":"AMZN","message":"Current price: $216.1"}
            return json.loads(response.text).get("message")
        except requests.exceptions.RequestException as e:
            print(f"Request failed: {e}")
            return response.text

    async def process_tool_async(self, tool_name, tool_content):
        """Process a tool call asynchronously and return the result"""
        # Create a unique task ID
        task_id = str(uuid.uuid4())

        # Create and store the task
        task = asyncio.create_task(self._run_tool(tool_name, tool_content))
        self.tasks[task_id] = task

        try:
            # Wait for the task to complete
            result = await task
            return result
        finally:
            # Clean up the task reference
            if task_id in self.tasks:
                del self.tasks[task_id]


    async def _run_tool(self, tool_name, tool_content):
        """Internal method to execute the tool logic"""
        print(f"== Processing tool: {tool_name}")
        tool = tool_name.lower()

        if tool == "getdateandtimetool":
            # Get current date in IST timezone
            pst_timezone = pytz.timezone("Asia/Kolkata")
            pst_date = datetime.datetime.now(pst_timezone)

            return {
                "formattedTime": pst_date.strftime("%I:%M %p"),
                "date": pst_date.strftime("%Y-%m-%d"),
                "year": pst_date.year,
                "month": pst_date.month,
                "day": pst_date.day,
                "dayOfWeek": pst_date.strftime("%A").upper(),
                "timezone": "IST"
            }

        elif tool == "getstockvaluetool":
            content = tool_content.get("content", {})
            content_data = json.loads(content)
            company_name = content_data.get("companyName", "")

            try:
                endpoint = 'https://awspe.com/api/price?stock='+company_name
                stock_value = self.perform_curl(endpoint)
                return {
                    "company_name": company_name,
                    "stock_value": stock_value
                }
            except Exception as e:
                endpoint = 'https://awspe.com/api/price?stock=amazon'
                stock_value = self.perform_curl(endpoint)
                return {
                    "company_name": company_name,
                    "stock_value": stock_value
                }

        elif tool == "trackpaymenttool":
            # Simulate a long-running operation
            print(f"TrackPaymentTool starting operation that will take time...")
            await asyncio.sleep(1)  # Non-blocking sleep to simulate processing time

            # Extract payment ID from toolUseContent
            content = tool_content.get("content", {})
            content_data = json.loads(content)
            payment_id = content_data.get("paymentId", "")
            request_notifications = content_data.get("requestNotifications", False)

            print(f"Payment ID: {payment_id}")

            # Convert payment_id to string if it's an integer
            if isinstance(payment_id, int):
                payment_id = str(payment_id)
            # Validate payment ID format
            if not payment_id or not isinstance(payment_id, str):
                return {
                    "error": "Invalid payment ID format",
                    "paymentStatus": "",
                    "estimatedDelivery": "",
                    "lastUpdate": ""
                }

            # Create deterministic randomness based on payment ID
            # This ensures the same payment ID always returns the same status
            seed = int(hashlib.md5(payment_id.encode(), usedforsecurity=False).hexdigest(), 16) % 10000
            random.seed(seed)

            # Rest of the payment tracking logic
            statuses = [
                "Payment Initiated",
                "Awaiting UPI App Approval",
                "User Approved Payment",
                "Verifying Payment",
                "Payment Successful",
                "Payment Failed",
                "Refund Initiated",
                "Refund Processed",
                "Payment Timeout",
                "UPI App Closed by User"
            ]

            weights = [10, 20, 15, 10, 25, 8, 5, 3, 3, 1]
            status = random.choices(statuses, weights=weights, k=1)[0]

            # Generate processing timestamp logic
            now = datetime.datetime.now()
            if status in ["Payment Successful", "Payment Failed"]:
                processed_time = (now - datetime.timedelta(minutes=random.randint(1, 30))).strftime("%Y-%m-%d %H:%M:%S")
            else:
                eta_minutes = random.randint(1, 10)
                estimated_processing_time = (now + datetime.timedelta(minutes=eta_minutes)).strftime(
                    "%Y-%m-%d %H:%M:%S")

            # Handle notification request
            notification_message = ""
            if request_notifications and status not in ["Payment Successful", "Payment Failed"]:
                notification_message = f"You will receive updates for payment {payment_id}"

            # Return tracking information
            tracking_info = {
                "paymentStatus": status,
                "paymentNumber": payment_id,
                "notificationStatus": notification_message
            }

            # Add timestamps
            if status in ["Payment Successful", "Payment Failed"]:
                tracking_info["processedAt"] = processed_time
            else:
                tracking_info["estimatedProcessingBy"] = estimated_processing_time

            # Add location-like simulation (optional, e.g., UPI backend node)
            if status == "Verifying Payment":
                tracking_info["processingNode"] = "Bank Gateway"
            elif status == "Awaiting UPI App Approval":
                tracking_info["awaitingAt"] = "User’s UPI App"
            elif status == "Refund Initiated":
                tracking_info["refundStatus"] = "Processing by bank"
            elif status == "Refund Processed":
                tracking_info["refundedAt"] = processed_time

            # Add extra detail for timeout or user cancel
            if status == "Payment Timeout":
                tracking_info["additionalInfo"] = "The payment session expired without user action"
            elif status == "UPI App Closed by User":
                tracking_info["additionalInfo"] = "The user exited the UPI app before completing the transaction"

            print("TrackUPIPaymentTool completed successfully")
            return tracking_info

        elif tool == "knowledgebase":
            content = tool_content.get("content", {})
            content_data = json.loads(content)
            print(content_data)
            result = knowledge_base.retrieve_and_generation(content_data['query'])
            return {"result": result}

        else:
            return {
                "error": f"Unsupported tool: {tool_name}"
            }