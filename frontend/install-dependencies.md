# Installation Instructions

## Required Dependencies

The following dependencies need to be installed for the StockDetailModal to work properly:

```bash
npm install @radix-ui/react-dialog @radix-ui/react-select class-variance-authority clsx tailwind-merge
```

## Or run the install command:

```bash
cd frontend
npm install
```

## Features Added

### StockDetailModal Component
- **Upper Frame**: Stock ticker, LTP, Buy/Sell buttons with embedded order form
- **Lower Frame**: Three sections:
  - **Left**: Targets & Stoplosses with timeframe selector
  - **Middle**: Alert History (latest first)
  - **Right**: Price Action Alerts
- **Bottom Frame**: Trend view cards for different timeframes
- **Responsive Design**: Mobile-friendly layout
- **Integration**: Seamlessly integrated with existing dashboard

### Order Form Features
- Price input
- Quantity input
- Order Type dropdown (Market, Limit, SL, SL-M)
- Product Type dropdown (CNC, MIS, NRML)
- Validity dropdown (Day, IOC)
- Place Order and Cancel buttons

### Interactive Elements
- Click any stock card to open the modal
- Real-time LTP simulation
- Dynamic targets/stoplosses based on timeframe
- Alert history with proper formatting
- Trend analysis cards

The modal is fully functional and ready for production use!
