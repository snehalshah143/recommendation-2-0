import React from 'react';
import RecommendationDashboardNew from './components/RecommendationDashboardNew';

function App() {
  return (
    <div className="App">
      <RecommendationDashboardNew 
        apiBaseUrl="http://localhost:8082" 
      />
    </div>
  );
}

export default App;
