import { useState, useMemo } from "react";
import { Doughnut } from "react-chartjs-2";
import { Chart as ChartJS } from "chart.js/auto";
import "./RunHistory.css";
import { DayPicker } from "react-day-picker";
import "react-day-picker/dist/style.css";

const RunHistory = (data) => {
  const [filters, setFilters] = useState({
    status: "",
    dateRange: { from: "", to: "" },
  });

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
      status: "",
      dateRange: { from: "", to: "" },
    };
    setFilters(resetFilters);
  };

  // Filter the data based on current filters (dynamically)
  const filteredData = useMemo(() => {
    if (!data.props) return [];

    return data.props.filter((item) => {
      // Status filter
      if (filters.status) {
        const itemStatus =
          item.executionStage === "FINISHED" ||
          item.executionStage === "IN_PROGRESS"
            ? "Finished"
            : "Failed";
        if (itemStatus !== filters.status) return false;
      }

      // Date range filter
      if (filters.dateRange.from || filters.dateRange.to) {
        const itemDate = new Date(item.startedAt);

        if (filters.dateRange.from) {
          const startDate = new Date(filters.dateRange.from);
          if (itemDate < startDate) return false;
        }

        if (filters.dateRange.to) {
          const endDate = new Date(filters.dateRange.to);
          endDate.setHours(23, 59, 59, 999); // Include the entire end date
          if (itemDate > endDate) return false;
        }
      }

      return true;
    });
  }, [data.props, filters]);

  // Calculate stats for donut charts
  const calculateStats = (days) => {
    const cutoffDate = new Date();
    cutoffDate.setHours(0, 0, 0, 0); // Set time to start of the day
    cutoffDate.setDate(cutoffDate.getDate() - days);
    let finished = 0;
    let failed = 0;

    if (data.props) {
      data.props.forEach((item) => {
        const itemDate = new Date(item.startedAt);
        if (itemDate >= cutoffDate) {
          if (item.executionStage === "FINISHED") {
            finished += 1;
          } else if (item.executionStage === "FAILED") {
            failed += 1;
          }
        }
      });
    }
    return { finished, failed };
  };

  const statsToday = calculateStats(0);
  const statsWeek = calculateStats(7);
  const statsMonth = calculateStats(30);

  // Donut chart data configuration
  const createDonutData = (stats) => ({
    labels: ["Finished", "Failed"],
    datasets: [
      {
        data: [stats.finished, stats.failed],
        backgroundColor: ["rgb(83, 221, 28)", "rgb(231, 12, 12)"],
        borderColor: "transparent",
        hoverOffset: 8,
      },
    ],
  });

  // Chart options
  const chartOptions = {
    maintainAspectRatio: false,
    cutout: "60%",
    animation: {
      duration: 1000,
    },
    plugins: {
      legend: {
        display: false,
      },
    },
  };

  return (
    <div className="main-content app-content">
      <div className="container">
        <div className="row my-4">
          <div className="col page-header ps-0">
            <h5>
              <i className="bi-clock-history fs-6 primary-color"></i> Run
              History
            </h5>
          </div>
        </div>

        <div className="row justify-content-between">
          <div className="col-l d-flex flex-column gap-3">
            <div className="row card">
              <div class="accordion">
                <div class="accordion-item">
                  <h2 class="accordion-header" id="headingOne">
                    <button
                      class="accordion-button collapsed"
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
                    class="accordion-collapse collapse"
                    aria-labelledby="headingOne"
                    data-bs-parent="#accordionExample"
                  >
                    <div class="accordion-body d-flex flex-column gap-3">
                      <div class="row justify-content-around">
                        <div className="col-3">Status</div>
                        <div className="col-6 d-flex flex-row gap-4">
                          <div class="form-check form-check-inline">
                            <input
                              class="form-check-input"
                              type="radio"
                              name="inlineRadioOptions"
                              id="inlineRadio1"
                              value="Finished"
                              onChange={(e) =>
                                handleFilterChange("status", e.target.value)
                              }
                              checked={filters.status === "Finished"}
                            />
                            <label class="form-check-label" for="inlineRadio1">
                              Finished
                            </label>
                          </div>
                          <div class="form-check form-check-inline">
                            <input
                              class="form-check-input"
                              type="radio"
                              name="inlineRadioOptions"
                              id="inlineRadio2"
                              value="Failed"
                              onChange={(e) =>
                                handleFilterChange("status", e.target.value)
                              }
                              checked={filters.status === "Failed"}
                            />
                            <label class="form-check-label" for="inlineRadio2">
                              Failed
                            </label>
                          </div>
                        </div>
                      </div>
                      <div class="row justify-content-around">
                        <div className="col-3">Date Range</div>
                        <div className="col-6 filter-datepicker">
                          <DayPicker
                            mode="range"
                            selected={filters.dateRange}
                            captionLayout="dropdown"
                            onSelect={(range) =>
                              handleFilterChange("dateRange", range)
                            }
                          />
                        </div>
                      </div>
                      <button
                        type="button"
                        id="resetBtn"
                        class="btn btn-secondary"
                        onClick={handleReset}
                      >
                        Reset
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            {filteredData.map((item) => (
              <a href={`/${item.id}`}>
                <div className="row card">
                  <div className="card-body ms-1 d-flex flex-column gap-2 pb-0">
                    <div className="run-title">
                      {item.executionStage === "FINISHED" && (
                        <i className="bi bi-check-circle-fill success-color"></i>
                      )}
                      {item.executionStage === "FAILED" && (
                        <i className="bi bi-x-circle-fill failed-color"></i>
                      )}
                      {item.executionStage === "IN_PROGRESS" && (
                        <span className="spinner-border spinner-border-sm text-primary"></span>
                      )}

                      <span className="ms-2 fw-semibold">Run #{item.id}</span>
                    </div>
                    <div className="run-details">
                      {(item.executionStage === "FINISHED" ||
                        item.executionStage === "FAILED") && (
                        <span className="small run-icon">
                          <i className="bi bi-hourglass me-2"></i>
                          {item.duration}
                        </span>
                      )}
                      {item.executionStage === "IN_PROGRESS" && (
                        <span className="small run-icon">
                          <i className="bi bi-hourglass-split me-2"></i>
                          In Progress
                        </span>
                      )}

                      <br></br>
                      <span className="small run-icon">
                        <i className="bi bi-calendar me-2"></i>
                        {item.startedAt}
                      </span>
                      {item.completedAt && (
                        <span className="small ms-1 run-icon">
                          - {item.completedAt}
                        </span>
                      )}
                    </div>
                  </div>
                  <div class="card-footer">
                    <span className="badge bg-outline-light">
                      {item.filterTag}
                    </span>
                  </div>
                </div>
              </a>
            ))}
          </div>
          <div className="col-s">
            <div className="sticky">
              <div className="card run-summary-card">
                <div className="card-header">
                  <h5 className="mt-1 mb-1">
                    <i className="bi-bar-chart-fill fs-6 primary-color"></i> Run
                    Summary
                  </h5>
                </div>
                <div className="card-body py-2">
                  <div className="text-center mb-3">
                    <div className="d-flex justify-content-center align-items-right">
                      <div className="d-flex align-items-center me-3">
                        <div className="finished-legend"></div>
                        <span style={{ fontSize: "11px", color: "#8b8b8b" }}>
                          Finished
                        </span>
                      </div>
                      <div className="d-flex align-items-center legend-item">
                        <div className="failed-legend"></div>
                        <span style={{ fontSize: "11px", color: "#8b8b8b" }}>
                          Failed
                        </span>
                      </div>
                    </div>
                  </div>

                  {statsToday.finished + statsToday.failed !== 0 && (
                    <div className="mb-4">
                      <h6 className="text-center mb-2 stats">Today</h6>
                      <div className="summary-doughnut">
                        <Doughnut
                          data={createDonutData(statsToday)}
                          options={chartOptions}
                        />
                      </div>
                      <p className="text-center mt-3 stats-tooltip">
                        Total Runs : {statsToday.finished + statsToday.failed}
                      </p>
                    </div>
                  )}

                  {statsWeek.finished + statsWeek.failed !== 0 && (
                    <div className="mb-4">
                      <h6 className="text-center mb-2 stats">Last Week</h6>
                      <div className="summary-doughnut">
                        <Doughnut
                          data={createDonutData(statsWeek)}
                          options={chartOptions}
                        />
                      </div>
                      <p className="text-center mt-3 stats-tooltip">
                        Total Runs : {statsWeek.finished + statsWeek.failed}
                      </p>
                    </div>
                  )}

                  {statsMonth.finished + statsMonth.failed !== 0 && (
                    <div className="mb-4">
                      <h6 className="text-center mb-2 stats">Last Month</h6>
                      <div className="summary-doughnut">
                        <Doughnut
                          data={createDonutData(statsMonth)}
                          options={chartOptions}
                        />
                      </div>
                      <p className="text-center mt-3 stats-tooltip">
                        Total Runs : {statsMonth.finished + statsMonth.failed}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RunHistory;
