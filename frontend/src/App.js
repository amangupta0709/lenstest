import React, { use, useEffect, useState } from 'react';
import Nav from './components/navbar';
import Builds from './components/builds';
import ReportDetails from './components/reportdetails';
import { useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

const fetchData = async () => {
  const res = await fetch('http://localhost:8080/api/tests/');
  const data = await res.json();
  return data;
}

const App = () => {
  const [results, setResults] = useState([]);
  const [parsedResults, setParsedResults] = useState([]);

  function deepParseJSON(value, maxDepth = 6, currentDepth = 0) {
    if (currentDepth >= maxDepth) return value;

    if (typeof value === 'string') {
      try {
        const parsed = JSON.parse(value);
        return deepParseJSON(parsed, maxDepth, currentDepth + 1);
      } catch {
        return value; // Not JSON
      }
    }

    if (Array.isArray(value)) {
      return value.map(item => deepParseJSON(item, maxDepth, currentDepth + 1));
    }

    if (typeof value === 'object' && value !== null) {
      const parsedObject = {};
      for (const [key, val] of Object.entries(value)) {
        parsedObject[key] = deepParseJSON(val, maxDepth, currentDepth + 1);
      }
      return parsedObject;
    }

    return value; // Primitive
  }


  // Fetch full result history on initial page load
  useEffect(() => {
      fetch("http://localhost:8080/api/tests/")
          .then((res) => res.json())
          .then((json) => {
              setResults(json);
          });
  }, []); 

  

  // Setup Server-Sent Events for live updates
  useEffect(() => {
    const eventSource = new EventSource('http://localhost:8080/api/tests/results/');

    eventSource.onmessage = (event) => {
      const update = JSON.parse(event.data);

      // Replace result with matching ID, or add if new
      setResults((prevResults) => {
        const index = prevResults.findIndex(r => r.id === update.id);
        if (index !== -1) {
          const updatedResults = [...prevResults];
          updatedResults[index] = { ...updatedResults[index], ...update };
          return updatedResults;
        } else {
          return [...prevResults, update];
        }
      });
    };

    eventSource.onerror = (error) => {
      console.error('SSE error:', error);
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, []);

  useEffect(() => {
    // Log the results whenever they change
    const parsed = results.map(r => ({
      ...r,
      tagSummaryJson: deepParseJSON(r.tagSummaryJson),
      testDetails: deepParseJSON(r.testDetails),
      statsSummaryJson: deepParseJSON(r.statsSummaryJson)
    }));
    setParsedResults(parsed);
  }, [results]);

  console.log('Parsed Results:', parsedResults);

  const navbarProps = {};
  navbarProps['durationPretty'] = results.length > 0 ? results[0].durationPretty : 'Loading...';

  const buildProps = useMemo(() => {
    if (!results) return [];

    return parsedResults
      .sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt)) // Sort by startedAt descending
      .map(item => ({
        id: item.id,
        startedAt: new Date(item.startedAt).toLocaleString('en-US', {
          month: 'short',
          day: '2-digit',
          year: 'numeric',
          hour: 'numeric',
          minute: '2-digit',
          hour12: true
        }).replace(/,/g, ''),
        completedAt: item.completedAt ? new Date(item.completedAt).toLocaleString('en-US', {
          month: 'short',
          day: '2-digit',
          year: 'numeric',
          hour: 'numeric',
          minute: '2-digit',
          hour12: true
        }).replace(/,/g, '') : null,
        durationPretty: item.durationPretty,
        executionStage: item.executionStage,
        tags: (() => {
          try {
            const parsed = JSON.parse(item.tagSummaryJson);
            return Object.keys(parsed); // Extract tag keys
          } catch (e) {
            console.error('Failed to parse tagSummaryJson for item', item.id, e);
            return [];
          }
        })()
      }));
  }, [results]);

  const ReportDetailsWrapper = () => {
    const { id } = useParams();
    const reportItem = parsedResults.find(r => String(r.id) === id);
    return <ReportDetails {...reportItem} />;
  };

  return (
    <body data-bs-theme="dark">
      <Router>
        <Nav props={navbarProps} />
        <Routes>
          <Route path="/" element={<Builds props={buildProps}/>} />
          <Route path="/report/:id" element={<ReportDetailsWrapper />} />
        </Routes>
      </Router>
    </body>
  );
};

export default App;
