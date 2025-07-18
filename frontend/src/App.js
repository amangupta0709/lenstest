import React, { use, useEffect, useState, useMemo} from 'react';
import Nav from './components/Navbar/Navbar';
import RunHistory from './components/RunHistory/RunHistory';
import { useParams } from 'react-router-dom';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import RunDetail from './components/RunDetail/RunDetail';
import './App.css';
import parsedDate from './components/utils/DateParser';

const App = () => {

  const [results, setResults] = useState([]);
  // const [parsedResults, setParsedResults] = useState([]);

  // function deepParseJSON(value, maxDepth = 6, currentDepth = 0) {
  //   if (currentDepth >= maxDepth) return value;

  //   if (typeof value === 'string') {
  //     try {
  //       const parsed = JSON.parse(value);
  //       return deepParseJSON(parsed, maxDepth, currentDepth + 1);
  //     } catch {
  //       return value; // Not JSON
  //     }
  //   }

  //   if (Array.isArray(value)) {
  //     return value.map(item => deepParseJSON(item, maxDepth, currentDepth + 1));
  //   }

  //   if (typeof value === 'object' && value !== null) {
  //     const parsedObject = {};
  //     for (const [key, val] of Object.entries(value)) {
  //       parsedObject[key] = deepParseJSON(val, maxDepth, currentDepth + 1);
  //     }
  //     return parsedObject;
  //   }

  //   return value; // Primitive
  // }


  // Fetch full result history on initial page load
  useEffect(() => {

      fetch("http://localhost:8080/api/tests/")
          .then((res) => res.json())
          .then((json) => {
            console.log('Fetched Results:', json);
              setResults(json);
          });
  }, []); 

  

  // Setup Server-Sent Events for live updates
  useEffect(() => {
    const eventSource = new EventSource('http://localhost:8080/api/tests/results');

    eventSource.onmessage = (event) => {
      const update = JSON.parse(event.data);
      console.log('SSE Update:', update);

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

  const navbarProps = {};
  navbarProps['durationPretty'] = results.length > 0 ? results[0].durationPretty : 'Loading...';

  const buildProps = useMemo(() => {

    if (!results) return [];

    return results
      .sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt)) // Sort by startedAt descending
      .map(item => ({
        id: item.id,
        startedAt: parsedDate(item.startedAt),
        completedAt: item.completedAt ? parsedDate(item.completedAt) : null,
        duration: item.duration,
        executionStage: item.executionStage,
        filterTag: item.filterTag,
      }));
  }, [results]);

  const ReportDetailsWrapper = () => {
    const { id } = useParams();
    const reportItem = results.find(r => String(r.id) === id);
    return <RunDetail {...reportItem} />;
  };

  return (
    <body>
      <Router>
        <Nav props={navbarProps} />
        <Routes>
          <Route path="/" element={<RunHistory props={buildProps}/>} />
          <Route path="/:id" element={<ReportDetailsWrapper />} />
        </Routes>
      </Router>
    </body>
  );
};

export default App;
