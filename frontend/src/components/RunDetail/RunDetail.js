import React, { useEffect, useState, useMemo } from "react";
import { Doughnut } from "react-chartjs-2";
import { Chart as ChartJS } from "chart.js/auto";
import "./RunDetail.css";
import parsedDate from "../utils/DateParser";
import { Link } from "react-router-dom";

const RunDetail = (data) => {
  console.log("Data", data);

  const [showSummary, setShowSummary] = useState(true);
  const [tagOptions, setTagOptions] = useState([]);
  const [selectedFeature, setSelectedFeature] = useState(null);
  const [selectedScenario, setSelectedScenario] = useState(null);
  const [filters, setFilters] = useState({
    featureFilter: "",
    scenarioFilter: "",
    tagFilter: "",
  });

  const chartData = {
    labels: ["Passed", "Failed", "Skipped"],
    datasets: [
      {
        backgroundColor: [
          "rgb(4, 207, 45)",
          "rgb(230, 9, 9)",
          "rgba(175, 174, 174, 0.77)",
        ],
        borderColor: "transparent",
        hoverOffset: 8,
      },
    ],
  };
  const chartOptions = {
    maintainAspectRatio: false,
    cutout: "70%",
    animation: {
      duration: 2000,
    },
    plugins: {
      legend: {
        position: "right",
        labels: {
          boxWidth: 15,
        },
      },
    },
  };

  // Handle Tags in dropdown
  useEffect(() => {
    if (data && data.tagStats) {
      setTagOptions(
        Object.keys(data.tagStats).map((key) => (
          <li>
            <button
              value={key}
              className="dropdown-item"
              onClick={(e) =>
                setFilters({ ...filters, tagFilter: e.target.value })
              }
            >
              {key}
            </button>
          </li>
        ))
      );
    }
  }, [data]);

  // Handle filter input changes - updates filters and list dynamically
  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  // Handle reset button click
  const handleReset = () => {
    const resetFilters = {
      featureFilter: "",
      scenarioFilter: "",
      tagFilters: "",
    };
    setFilters(resetFilters);
  };

  // Filter the data based on current filters (dynamically)
  const filteredData = useMemo(() => {
    return (
      // Features filter
      data?.features &&
      Object.entries(data?.features)
        .map(([key, feature]) => {
          // Scenarios filters
          const filteredScenarios = feature.scenarios?.filter((scenario) => {
            if (filters.scenarioFilter) {
              const scenarioFilter =
                scenario.status === "PASSED" ||
                scenario.status === "IN_PROGRESS"
                  ? "PASSED"
                  : "FAILED";

              if (scenarioFilter !== filters.scenarioFilter) return false;
            }

            if (filters.tagFilter) {
              return scenario.tags.some((tag) => tag === filters.tagFilter);
            }
            return true;
          });
          return { ...feature, scenarios: filteredScenarios };
        })
        .filter((feature) => {
          const featureFilter =
            feature.status === "PASSED" || feature.status === "IN_PROGRESS"
              ? "PASSED"
              : "FAILED";

          if (filters.featureFilter && featureFilter !== filters.featureFilter)
            return false;
          if (filters.scenarioFilter === "FAILED" && featureFilter !== "FAILED")
            return false;

          return feature.scenarios.length !== 0;
        })
    );
  }, [data, filters]);

  const handleDownload = () => {
    const element = document.getElementById("root"); // your component's container

    if (!element) {
      alert("Snapshot element not found");
      return;
    }

    // Inline styles
    const styles = Array.from(document.styleSheets)
      .map((sheet) => {
        try {
          return Array.from(sheet.cssRules)
            .map((rule) => rule.cssText)
            .join("\n");
        } catch {
          return ""; // ignore cross-origin stylesheets
        }
      })
      .join("\n");

    const html = `
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <title>${document.title}</title>
      <style>${styles}</style>
    </head>
    <body>
      <div id="root">
        ${element.innerHTML}
      </div>
    </body>
    </html>
    `;

    const blob = new Blob([html], { type: "text/html" });
    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.download = `run-report.html`;
    link.click();

    URL.revokeObjectURL(url);
  };

  return (
    <div className="container d-flex flex-column gap-4 pb-5">
      <div className="row mt-4">
        <div className="col page-header ps-0">
          <h5>
            {data.executionStage === "FINISHED" && (
              <i className="bi bi-check-circle-fill success-color"></i>
            )}
            {data.executionStage === "FAILED" && (
              <i className="bi bi-x-circle-fill failed-color"></i>
            )}
            {data.executionStage === "IN_PROGRESS" && (
              <span className="spinner-border spinner-border-sm text-primary"></span>
            )}{" "}
            Run #{data.id}
          </h5>
        </div>
        <div className="col pe-0 d-flex justify-content-end">
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={(e) => setShowSummary(!showSummary)}
          >
            <i className="bi bi-bar-chart-fill me-2"></i>
            {showSummary ? "Hide " : "Show "} Summary
          </button>
        </div>
        {/* <div className="col pe-0 d-flex justify-content-end">
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={(e) => handleDownload()}
          >
            <i className="bi bi-bar-chart-fill me-2"></i>
            Download
          </button>
        </div> */}
      </div>
      {showSummary && (
        <div className="test-summary">
          <div className="d-flex flex-column gap-4">
            <div className="row gap-4">
              <div className="col card px-0">
                <div className="card-header py-3">
                  <h6 className="px-2">Features</h6>
                </div>
                <div className="card-body py-4">
                  <div className="d-flex justify-content-center">
                    <Doughnut
                      data={{
                        ...chartData,
                        datasets: [
                          {
                            ...chartData.datasets[0],
                            data: [
                              data?.featureStats?.passed,
                              data?.featureStats?.failed,
                              data?.featureStats?.skipped,
                            ],
                          },
                        ],
                      }}
                      options={chartOptions}
                    />
                  </div>
                </div>
                <div className="card-footer py-3">
                  <div className="px-2">
                    {data?.featureStats?.passed} Passed,{" "}
                    {data?.featureStats?.failed} Failed,{" "}
                    {data?.featureStats?.skipped} Skipped
                  </div>
                </div>
              </div>

              <div className="col card px-0">
                <div className="card-header py-3">
                  <h6 className="px-2">Scenarios</h6>
                </div>
                <div className="card-body py-4">
                  <div className="d-flex justify-content-center">
                    <Doughnut
                      data={{
                        ...chartData,
                        datasets: [
                          {
                            ...chartData.datasets[0],
                            data: [
                              data?.scenarioStats?.passed,
                              data?.scenarioStats?.failed,
                              data?.scenarioStats?.skipped,
                            ],
                          },
                        ],
                      }}
                      options={chartOptions}
                    />
                  </div>
                </div>
                <div className="card-footer py-3">
                  <div className="px-2">
                    {data?.scenarioStats?.passed} Passed,{" "}
                    {data?.scenarioStats?.failed} Failed,{" "}
                    {data?.scenarioStats?.skipped} Skipped
                  </div>
                </div>
              </div>

              <div className="col card px-0">
                <div className="card-header py-3">
                  <h6 className="px-2">Steps</h6>
                </div>
                <div className="card-body py-4">
                  <div className="d-flex justify-content-center">
                    <Doughnut
                      data={{
                        ...chartData,
                        datasets: [
                          {
                            ...chartData.datasets[0],
                            data: [
                              data?.stepStats?.passed,
                              data?.stepStats?.failed,
                              data?.stepStats?.skipped,
                            ],
                          },
                        ],
                      }}
                      options={chartOptions}
                    />
                  </div>
                </div>
                <div className="card-footer py-3">
                  <div className="px-2">
                    {data?.stepStats?.passed} Passed, {data?.stepStats?.failed}{" "}
                    Failed, {data?.stepStats?.skipped} Skipped
                  </div>
                </div>
              </div>
            </div>

            <div className="row">
              <div className="col card px-0">
                <div className="card-header py-3">
                  <h6 className="px-2">Tags</h6>
                </div>
                <div className="card-body py-4">
                  <table className="table tags-table px-5">
                    <thead>
                      <tr>
                        <th></th>
                        <th>Total</th>
                        <th>Passed</th>
                        <th>Failed</th>
                        <th>Skipped</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data?.tagStats &&
                        Object.entries(data.tagStats).map(([key, value]) => (
                          <tr key={key}>
                            <td>{key}</td>
                            <td>{value.total}</td>
                            <td>{value.passed}</td>
                            <td>{value.failed}</td>
                            <td>{value.skipped}</td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
      <div className="test-details">
        <div className="d-flex flex-column gap-4">
          <div className="row">
            <div className="card">
              <div className="accordion">
                <div className="accordion-item">
                  <h2 className="accordion-header" id="headingOne">
                    <button
                      className="accordion-button collapsed"
                      type="button"
                      data-bs-toggle="collapse"
                      data-bs-target="#collapseOne"
                      aria-expanded="true"
                      aria-controls="collapseOne"
                    >
                      <i className="bi bi-search me-3"></i>
                      Filter
                    </button>
                  </h2>
                  <div
                    id="collapseOne"
                    className="accordion-collapse collapse"
                    aria-labelledby="headingOne"
                    data-bs-parent="#accordionExample"
                  >
                    <div className="accordion-body d-flex flex-column gap-4">
                      <div className="row justify-content-center">
                        <div className="col-3">Features</div>
                        <div className="col-3 d-flex flex-row justify-content-between">
                          <div className="form-check form-check-inline">
                            <input
                              className="form-check-input filter-radio-button"
                              type="radio"
                              name="features"
                              value="PASSED"
                              onChange={(e) =>
                                handleFilterChange(
                                  "featureFilter",
                                  e.target.value
                                )
                              }
                              checked={filters.featureFilter === "PASSED"}
                            />
                            <label className="form-check-label">Passed</label>
                          </div>
                          <div className="form-check form-check-inline">
                            <input
                              className="form-check-input filter-radio-button"
                              type="radio"
                              name="features"
                              value="FAILED"
                              onChange={(e) =>
                                handleFilterChange(
                                  "featureFilter",
                                  e.target.value
                                )
                              }
                              checked={filters.featureFilter === "FAILED"}
                            />
                            <label className="form-check-label">Failed</label>
                          </div>
                        </div>
                      </div>
                      <div className="row justify-content-center">
                        <div className="col-3">Scenarios</div>
                        <div className="col-3 d-flex flex-row justify-content-between">
                          <div className="form-check form-check-inline">
                            <input
                              className="form-check-input filter-radio-button"
                              type="radio"
                              name="scenarios"
                              value="PASSED"
                              onChange={(e) =>
                                handleFilterChange(
                                  "scenarioFilter",
                                  e.target.value
                                )
                              }
                              checked={filters.scenarioFilter === "PASSED"}
                            />
                            <label className="form-check-label">Passed</label>
                          </div>
                          <div className="form-check form-check-inline">
                            <input
                              className="form-check-input filter-radio-button"
                              type="radio"
                              name="scenarios"
                              value="FAILED"
                              onChange={(e) =>
                                handleFilterChange(
                                  "scenarioFilter",
                                  e.target.value
                                )
                              }
                              checked={filters.scenarioFilter === "FAILED"}
                            />
                            <label className="form-check-label">Failed</label>
                          </div>
                        </div>
                      </div>
                      <div className="row justify-content-center">
                        <div className="col-3">Tags</div>
                        <div className="col-3 d-flex flex-column justify-content-center">
                          <div class="dropdown">
                            <button
                              class="btn btn-primary btn-sm dropdown-toggle"
                              type="button"
                              data-bs-toggle="dropdown"
                            >
                              {filters.tagFilter
                                ? filters.tagFilter
                                : "Select Tag"}
                            </button>
                            <ul class="dropdown-menu">{tagOptions}</ul>
                          </div>
                        </div>
                      </div>
                      <div className="row justify-content-center">
                        <button
                          type="button"
                          className="btn btn-secondary filter-reset-button"
                          onClick={handleReset}
                        >
                          Reset
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="row">
            <div className="card px-0">
              <div className="card-header py-3">
                <h6 className="px-2">Test Details</h6>
              </div>
              <div className="card-body p-0">
                {filteredData && filteredData.length > 0 ? (
                  <div className="row pe-4">
                    {/* Features List - Left Side */}
                    <div className="col-4 scroll-visible d-flex flex-column gap-2 mt-3 ps-4 mb-4">
                      {filteredData
                        .sort(
                          (a, b) =>
                            new Date(a.startedAt) - new Date(b.startedAt)
                        )
                        .map((feature, index) => (
                          <div
                            key={index}
                            type="button"
                            onClick={() => setSelectedFeature(index)}
                            className={`box py-3 ${
                              selectedFeature === index ? "selected" : ""
                            }`}
                          >
                            <div className="feature px-4">
                              <div className="feature-name">
                                <h6
                                  className={`${
                                    feature.status === "PASSED"
                                      ? "success-color"
                                      : "failed-color"
                                  }`}
                                >
                                  {feature.name}
                                </h6>
                              </div>
                              <div className="">
                                {feature.completedAt ? (
                                  <span className="small run-icon">
                                    <i className="bi bi-hourglass me-2"></i>
                                    {feature.duration}
                                  </span>
                                ) : (
                                  <span className="small run-icon">
                                    <i className="bi bi-hourglass-split me-2"></i>
                                    In Progress
                                  </span>
                                )}

                                <br></br>

                                <span className="small run-icon">
                                  <i className="bi bi-calendar me-2"></i>
                                  {parsedDate(feature.startedAt)}
                                </span>
                                {feature.completedAt && (
                                  <span className="small ms-1 run-icon">
                                    - {parsedDate(feature.completedAt)}
                                  </span>
                                )}
                              </div>
                            </div>
                          </div>
                        ))}
                    </div>

                    {/* Scenarios List - Right Side */}
                    <div className="col-8 mt-3 scroll-visible">
                      {selectedFeature !== null &&
                      filteredData[selectedFeature] ? (
                        <div className="d-flex flex-column gap-2 pb-4">
                          <div className="row">
                            <h6 className="feature-heading">
                              {filteredData[selectedFeature].name}
                            </h6>
                            <h6 className="feature-description">
                              {filteredData[selectedFeature].description}
                            </h6>
                          </div>
                          {Object.entries(
                            filteredData[selectedFeature].scenarios || {}
                          ).map(([scenarioKey, scenario]) => (
                            <Link
                              key={scenarioKey}
                              type="button"
                              to={`/${data.id}/scenario/${scenario.id}`}
                              onClick={() => setSelectedScenario(scenarioKey)}
                              className={`box py-3 ${
                                selectedScenario === scenarioKey
                                  ? "selected"
                                  : ""
                              }`}
                            >
                              <div className="ps-4">
                                <h6
                                  className={`${
                                    scenario.status === "PASSED"
                                      ? "success-color"
                                      : "failed-color"
                                  }`}
                                >
                                  {scenario.name}
                                </h6>
                                <div className="">
                                  {scenario.tags &&
                                    scenario.tags.length > 0 &&
                                    scenario.tags.map((tag, index) => (
                                      <span
                                        key={index}
                                        className="badge badge-dark me-1 mb-1"
                                      >
                                        {tag}
                                      </span>
                                    ))}
                                </div>
                                <span className="small run-icon">
                                  <i className="bi bi-hourglass me-2"></i>
                                  {scenario.duration}
                                </span>
                                <br></br>
                                <span className="small run-icon">
                                  <i className="bi bi-calendar me-2"></i>
                                  {parsedDate(scenario.startedAt)}
                                </span>
                                {scenario.completedAt && (
                                  <span className="small ms-1 run-icon">
                                    - {parsedDate(scenario.completedAt)}
                                  </span>
                                )}
                              </div>
                            </Link>
                          ))}
                        </div>
                      ) : (
                        <div className="warning-text text-center py-5">
                          <i className="bi bi-arrow-left fs-1"></i>
                          <br></br>
                          Select a feature to view its scenarios
                        </div>
                      )}
                    </div>
                  </div>
                ) : (
                  <div className="warning-text text-center py-5">
                    <i className="bi bi-inbox fs-2"></i>
                    <br></br>
                    No features data available
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RunDetail;
