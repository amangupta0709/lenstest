import React, { use, useEffect, useState, useMemo } from "react";
import Nav from "./components/Navbar/Navbar";
import RunHistory from "./components/RunHistory/RunHistory";
import { BrowserRouter, useParams } from "react-router-dom";
import { HashRouter, Routes, Route } from "react-router-dom";
import RunDetail from "./components/RunDetail/RunDetail";
import "./App.css";
import parsedDate from "./components/utils/DateParser";
import ScenarioDetail from "./components/ScenarioDetail/ScenarioDetail";

const App = () => {
  const [results, setResults] = useState([]);

  // Fetch full result history on initial page load
  useEffect(() => {
    fetch("http://localhost:8080/api/tests/")
      .then((res) => res.json())
      .then((json) => {
        console.log("Fetched Results:", json);
        setResults(json);
      });
  }, []);

  // Setup Server-Sent Events for live updates
  useEffect(() => {
    const eventSource = new EventSource(
      "http://localhost:8080/api/tests/results"
    );

    eventSource.onmessage = (event) => {
      const update = JSON.parse(event.data);
      console.log("SSE Update:", update);

      // Replace result with matching ID, or add if new
      setResults((prevResults) => {
        const index = prevResults.findIndex((r) => r.id === update.id);
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
      console.error("SSE error:", error);
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, []);

  const buildProps = useMemo(() => {
    if (results.error) return [];

    return results
      .sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt)) // Sort by startedAt descending
      .map((item) => ({
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
    const reportItem = results.find((r) => String(r.id) === id);
    return <RunDetail {...reportItem} />;
  };

  const ScenarioDetailsWrapper = () => {
    const { id, scenarioId } = useParams();
    const run = results.find((r) => String(r.id) === id);

    let selectedScenario = [];
    if (run?.features) {
      for (const feature of run.features) {
        const found = feature.scenarios?.find(
          (s) => String(s.id) === scenarioId
        );
        if (found) {
          selectedScenario = found;
          break;
        }
      }
    }

    return <ScenarioDetail {...selectedScenario} />;
  };

  const AppRouter = ({ children }) => {
    const Router = window.location.protocol === "file:" ? HashRouter : BrowserRouter;
    return <Router>{children}</Router>
  }

  return (
    <div>
      <AppRouter>
        <Nav />
        <Routes>
          <Route path="/" element={<RunHistory props={buildProps} />} />
          <Route path="/:id" element={<ReportDetailsWrapper />} />
          <Route
            path="/:id/scenario/:scenarioId"
            element={<ScenarioDetailsWrapper />}
          />
        </Routes>
      </AppRouter>
    </div>
  );
};

export default App;
