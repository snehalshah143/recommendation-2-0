import React from 'react';
import StockFundamentals from './StockFundamentals';

const StockFundamentalsExample = ({ baseUrl = '' }) => {
  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Stock Fundamentals Examples</h2>
      
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* RELIANCE Example */}
        <StockFundamentals symbol="RELIANCE" baseUrl={baseUrl} />
        
        {/* TCS Example */}
        <StockFundamentals symbol="TCS" baseUrl={baseUrl} />
        
        {/* HDFC Example */}
        <StockFundamentals symbol="HDFC" baseUrl={baseUrl} />
      </div>
    </div>
  );
};

export default StockFundamentalsExample;




