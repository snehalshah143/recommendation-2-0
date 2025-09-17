import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';

const SimpleCustomBasket = ({ 
  isOpen, 
  onClose, 
  customStocks, 
  onStocksChange 
}) => {
  const [stockInput, setStockInput] = useState(customStocks.join(', '));

  const handleSave = () => {
    // Parse the input - split by comma and clean up
    const stocks = stockInput
      .split(',')
      .map(stock => stock.trim().toUpperCase())
      .filter(stock => stock.length > 0)
      .map(stock => ({ symbol: stock, name: stock }));
    
    onStocksChange(stocks);
    onClose();
  };

  const handleCancel = () => {
    setStockInput(customStocks.join(', '));
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <Card className="w-96">
        <CardHeader>
          <CardTitle>Custom Basket - Add Stock List</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <label className="text-sm font-medium text-gray-700 mb-2 block">
              Enter stock symbols (comma separated):
            </label>
            <Input
              type="text"
              placeholder="RELIANCE, TCS, HDFC, INFY"
              value={stockInput}
              onChange={(e) => setStockInput(e.target.value)}
              className="w-full"
            />
            <p className="text-xs text-gray-500 mt-1">
              Example: RELIANCE, TCS, HDFC, INFY, WIPRO
            </p>
          </div>
          
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={handleCancel}>
              Cancel
            </Button>
            <Button onClick={handleSave} className="bg-blue-600 hover:bg-blue-700">
              Save Basket
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default SimpleCustomBasket;
