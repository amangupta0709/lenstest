import { useState, useMemo } from "react";
import { Doughnut } from "react-chartjs-2";
import "chart.js/auto";
import "./RunHistory.css";
import { DayPicker } from "react-day-picker";
import "react-day-picker/dist/style.css";
import { Link } from "react-router-dom";
import Select from "react-select";
import makeAnimated from "react-select/animated";
import ScheduledRun from "../ScheduledRun/ScheduledRun";

const RunHistory = ({ props, tagOptions }) => {
  const [newRun, setNewRun] = useState({
    includeTags: [],
    excludeTags: [],
  });

  const handleNewRunChange = (key, value) => {
    setNewRun((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const newRunSubmit = async () => {
    try {
      const url = "http://localhost:8080/api/tests/execute";
      await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newRun),
      });
    } catch (error) {
      console.error("Fetch error:", error);
    }
  };

  const [filters, setFilters] = useState({
    status: "",
    runType: "",
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
      runType: "",
      dateRange: { from: "", to: "" },
    };
    setFilters(resetFilters);
  };

  // Filter the data based on current filters (dynamically)
  const filteredData = useMemo(() => {
    if (!props) return [];

    return props.filter((item) => {
      // Status filter
      if (filters.status) {
        const itemStatus =
          item.executionStage === "FINISHED" ||
          item.executionStage === "IN_PROGRESS"
            ? "FINISHED"
            : "FAILED";
        if (itemStatus !== filters.status) return false;
      }

      if (filters.runType) {
        if (item.runType !== filters.runType) return false;
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
  }, [props, filters]);

  // Calculate stats for donut charts
  const calculateStats = (days) => {
    const cutoffDate = new Date();
    cutoffDate.setHours(0, 0, 0, 0); // Set time to start of the day
    cutoffDate.setDate(cutoffDate.getDate() - days);
    let finished = 0;
    let failed = 0;

    if (props) {
      props.forEach((item) => {
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
        backgroundColor: ["rgb(4, 207, 45)", "rgb(230, 9, 9)"],
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
      <div className="container pb-5">
        <div className="row my-4">
          <div className="col page-header ps-0 d-flex align-items-center">
            <h5 className="m-0">
              <i className="bi-list-ul fs-5 primary-color"></i> Run History
            </h5>
          </div>
          <div className="col d-flex justify-content-end gap-2">
            <button
              type="button"
              className="btn btn-outline-primary"
              data-bs-toggle="modal"
              data-bs-target="#scheduled-run-modal"
            >
              <i className="bi bi-clock-fill me-2"></i>
              Schedule Run
            </button>
            <button
              type="button"
              className="btn btn-primary"
              data-bs-toggle="modal"
              data-bs-target="#new-run-modal"
            >
              <i className="bi bi-gear-fill me-2"></i>
              New Run
            </button>
          </div>
        </div>

        <div className="row justify-content-between">
          <div className="col-l d-flex flex-column gap-4">
            <div className="row card">
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
                    <div className="accordion-body d-flex flex-column gap-3">
                      <div className="row justify-content-around">
                        <div className="col-3">Status</div>
                        <div className="col-6 d-flex flex-row gap-4">
                          <div className="form-check form-check-inline">
                            <input
                              className="form-check-input filter-radio-button"
                              type="radio"
                              name="inlineRadioOptionStatus"
                              id="inlineRadioStatus1"
                              value="FINISHED"
                              onChange={(e) =>
                                handleFilterChange("status", e.target.value)
                              }
                              checked={filters.status === "FINISHED"}
                            />
                            <label
                              className="form-check-label"
                              htmlFor="inlineRadioStatus1"
                            >
                              Finished
                            </label>
                          </div>
                          <div className="form-check form-check-inline">
                            <input
                              className="form-check-input filter-radio-button"
                              type="radio"
                              name="inlineRadioOptionStatus"
                              id="inlineRadioStatus2"
                              value="FAILED"
                              onChange={(e) =>
                                handleFilterChange("status", e.target.value)
                              }
                              checked={filters.status === "FAILED"}
                            />
                            <label
                              className="form-check-label"
                              htmlFor="inlineRadioStatus2"
                            >
                              Failed
                            </label>
                          </div>
                        </div>
                      </div>
                      <div className="row justify-content-around">
                        <div className="col-3">Run Type</div>
                        <div className="col-6 d-flex flex-row gap-4">
                          <div className="form-check form-check-inline">
                            <input
                              className="form-check-input filter-radio-button"
                              type="radio"
                              name="inlineRadioOptionType"
                              id="inlineRadioType1"
                              value="SCHEDULED"
                              onChange={(e) =>
                                handleFilterChange("runType", e.target.value)
                              }
                              checked={filters.runType === "SCHEDULED"}
                            />
                            <label
                              className="form-check-label"
                              htmlFor="inlineRadioType1"
                            >
                              Scheduled
                            </label>
                          </div>
                          <div className="form-check form-check-inline">
                            <input
                              className="form-check-input filter-radio-button"
                              type="radio"
                              name="inlineRadioOptionType"
                              id="inlineRadioType2"
                              value="MANUAL"
                              onChange={(e) =>
                                handleFilterChange("runType", e.target.value)
                              }
                              checked={filters.runType === "MANUAL"}
                            />
                            <label
                              className="form-check-label"
                              htmlFor="inlineRadioType2"
                            >
                              Manual
                            </label>
                          </div>
                        </div>
                      </div>
                      <div className="row justify-content-around">
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
            {filteredData.length > 0 ? (
              filteredData.map((item, index) => (
                <Link to={`/${item.id}`} key={index}>
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
                        {item.runType && (
                          <span className={`small run-icon`}>
                            {item.runType === "SCHEDULED" ? (
                              <i className="bi bi-clock-fill me-2"></i>
                            ) : (
                              <i className="bi bi-gear-fill me-2"></i>
                            )}
                            {item.runType === "SCHEDULED"
                              ? "Triggered by Scheduler"
                              : "Triggered Manually"}
                          </span>
                        )}
                        <br></br>
                        {item.scheduledRunId && (
                          <span
                            className="badge ms-1 bg-light text-dark"
                            title="Scheduled Run ID"
                          >
                            <i className="bi bi-link-45deg me-1"></i>
                            {item.scheduledRunId.substring(0, 8)}...
                          </span>
                        )}
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
                    <div className="card-footer run-filter-tag">
                      <span className="badge bg-outline-light">
                        {item.filterTag}
                      </span>
                    </div>
                  </div>
                </Link>
              ))
            ) : (
              <div className="warning-text text-center py-5">
                <i className="bi bi-inbox fs-1"></i>
                <br></br>
                No Runs found
              </div>
            )}
          </div>
          <div className="col-s">
            <div className="card run-summary-card">
              <div className="card-header mt-1">
                <h5 className="">
                  <i className="bi-bar-chart-fill fs-6 primary-color"></i> Run
                  Summary
                </h5>
              </div>
              <div className="card-body py-2 d-flex flex-column gap-3">
                {statsMonth.finished + statsMonth.failed !== 0 && (
                  <div className="row d-flex">
                    <div className="col d-flex align-items-center justify-content-center">
                      <div className="finished-legend"></div>
                      <span className="stats-tooltip">Finished</span>
                    </div>
                    <div className="col d-flex align-items-center justify-content-center">
                      <div className="failed-legend"></div>
                      <span className="stats-tooltip">Failed</span>
                    </div>
                  </div>
                )}

                {statsToday.finished + statsToday.failed !== 0 && (
                  <div>
                    <h6 className="text-center mb-3 stats">Today</h6>
                    <div className="summary-doughnut">
                      <Doughnut
                        data={createDonutData(statsToday)}
                        options={chartOptions}
                      />
                    </div>
                    <p className="text-center mt-2 stats-tooltip">
                      Total Runs : {statsToday.finished + statsToday.failed}
                    </p>
                  </div>
                )}

                {statsWeek.finished + statsWeek.failed !== 0 && (
                  <div>
                    <h6 className="text-center mb-3 stats">Last Week</h6>
                    <div className="summary-doughnut">
                      <Doughnut
                        data={createDonutData(statsWeek)}
                        options={chartOptions}
                      />
                    </div>
                    <p className="text-center mt-2 stats-tooltip">
                      Total Runs : {statsWeek.finished + statsWeek.failed}
                    </p>
                  </div>
                )}

                {statsMonth.finished + statsMonth.failed !== 0 && (
                  <div>
                    <h6 className="text-center mb-3 stats">Last Month</h6>
                    <div className="summary-doughnut">
                      <Doughnut
                        data={createDonutData(statsMonth)}
                        options={chartOptions}
                      />
                    </div>
                    <p className="text-center mt-2 stats-tooltip">
                      Total Runs : {statsMonth.finished + statsMonth.failed}
                    </p>
                  </div>
                )}

                {statsMonth.finished + statsMonth.failed === 0 && (
                  <div className="warning-text text-center py-5">
                    <i className="bi bi-inbox fs-1"></i>
                    <br></br>
                    No Runs found
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="modal fade" id="new-run-modal">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">
                <i className="bi bi-gear-fill me-2"></i>New Run
              </h5>
            </div>
            <div className="modal-body">
              {/* <input
                className="form-control"
                type="text"
                placeholder="Tag"
                onChange={(e) => handleNewRunChange("tag", e.target.value)}
              /> */}
              <div className="column d-flex flex-column gap-4">
                <div className="row">
                  <label className="select-label mb-2 fs-6 fw-bold">
                    Include Tags
                  </label>
                  <Select
                    isMulti
                    name="tagOptions"
                    closeMenuOnSelect={false}
                    onFocus={false}
                    components={makeAnimated()}
                    options={tagOptions}
                    className="basic-multi-select"
                    classNamePrefix="select"
                    onChange={(selected) => {
                      handleNewRunChange(
                        "includeTags",
                        selected?.map((opt) => opt.value) || []
                      );
                    }}
                  />
                </div>

                <div className="row">
                  <label className="select-label mb-2 fs-6 fw-bold">
                    Exclude Tags
                  </label>
                  <Select
                    isMulti
                    name="excludeTags"
                    closeMenuOnSelect={false}
                    components={makeAnimated()}
                    options={tagOptions}
                    className="basic-multi-select"
                    classNamePrefix="select"
                    onChange={(selected) => {
                      handleNewRunChange(
                        "excludeTags",
                        selected?.map((opt) => opt.value) || []
                      );
                    }}
                  />
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-secondary"
                data-bs-dismiss="modal"
                onClick={newRunSubmit}
                disabled={
                  newRun.includeTags.length === 0 &&
                  newRun.excludeTags.length === 0
                }
              >
                Submit
              </button>
            </div>
          </div>
        </div>
      </div>
      <ScheduledRun tagOptions={tagOptions} />
    </div>
  );
};

export default RunHistory;
