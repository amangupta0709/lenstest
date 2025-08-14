import { useEffect, useState } from "react";
import Select from "react-select";
import makeAnimated from "react-select/animated";
import "./ScheduledRun.css";
import parsedCron from "../utils/CronParser";

const ScheduledRun = ({ tagOptions }) => {
  const scheduleTypes = [
    {
      value: "daily",
      label: "Daily",
    },
    {
      value: "weekly",
      label: "Weekly",
    },
    {
      value: "custom",
      label: "Custom",
    },
  ];

  const daysOfWeek = [
    {
      value: "1",
      label: "Sunday",
    },
    {
      value: "2",
      label: "Monday",
    },
    {
      value: "3",
      label: "Tuesday",
    },
    {
      value: "4",
      label: "Wednesday",
    },
    {
      value: "5",
      label: "Thursday",
    },
    {
      value: "6",
      label: "Friday",
    },
    {
      value: "7",
      label: "Saturday",
    }
  ];
  const [scheduledRun, setScheduledRun] = useState({
    name: "",
    includeTags: [],
    excludeTags: [],
    scheduleType: "daily", // daily, weekly, custom
    time: "09:00",
    dayOfWeek: "1", // 1=Monday, 7=Sunday
    cronExpression: "",
  });

  const [scheduledRuns, setScheduledRuns] = useState([]);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const handleScheduledRunChange = (key, value) => {
    setScheduledRun((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  // Generate cron expression from user-friendly inputs
  const generateCronExpression = () => {
    const [hours, minutes] = scheduledRun.time.split(":");

    switch (scheduledRun.scheduleType) {
      case "daily":
        return `0 ${minutes} ${hours} * * ?`;
      case "weekly":
        return `0 ${minutes} ${hours} ? * ${scheduledRun.dayOfWeek}`;
      case "custom":
        return scheduledRun.cronExpression;
      default:
        return `0 ${minutes} ${hours} * * ?`;
    }
  };

  // Load scheduled runs
  const loadScheduledRuns = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/tests/scheduled");
      const data = await response.json();
      setScheduledRuns(data);
    } catch (error) {
      console.error("Error loading scheduled runs:", error);
    }
  };

  // Load scheduled runs when modal opens
  useEffect(() => {
    const modalElement = document.getElementById("scheduled-run-modal");
    if (modalElement) {
      modalElement.addEventListener("show.bs.modal", loadScheduledRuns);
      return () =>
        modalElement.removeEventListener("show.bs.modal", loadScheduledRuns);
    }
  }, []);

  const scheduledRunSubmit = async () => {
    try {
      const cronExpression = generateCronExpression();
      const payload = {
        ...scheduledRun,
        cronExpression,
      };

      const url = isEditing
        ? `http://localhost:8080/api/tests/scheduled/${editingId}`
        : "http://localhost:8080/api/tests/scheduled";

      const method = isEditing ? "PUT" : "POST";

      await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      // Reset form after successful submission
      resetScheduledRunForm();
      loadScheduledRuns();
    } catch (error) {
      console.error("Fetch error:", error);
      alert(`Error ${isEditing ? "updating" : "creating"} scheduled run`);
    }
  };

  const resetScheduledRunForm = () => {
    setScheduledRun({
      name: "",
      includeTags: [],
      excludeTags: [],
      scheduleType: "daily",
      time: "09:00",
      dayOfWeek: "1",
      cronExpression: "",
    });
    setIsEditing(false);
    setEditingId(null);
  };

  const editScheduledRun = (run) => {
    setScheduledRun({
      name: run.name,
      includeTags: run.includeTags || [],
      excludeTags: run.excludeTags || [],
      scheduleType: "custom", // For existing runs, show as custom
      time: "09:00",
      dayOfWeek: "1",
      cronExpression: run.cronExpression,
    });
    setIsEditing(true);
    setEditingId(run.id);
  };

  const deleteScheduledRun = async (id) => {
    try {
      await fetch(`http://localhost:8080/api/tests/scheduled/${id}`, {
        method: "DELETE",
      });
      loadScheduledRuns();
    } catch (error) {
      console.error("Error deleting scheduled run:", error);
      alert("Error deleting scheduled run");
    }
  };

  const toggleScheduledRun = async (id, active) => {
    try {
      await fetch(
        `http://localhost:8080/api/tests/scheduled/${id}/toggle?active=${!active}`,
        {
          method: "PUT",
        }
      );
      loadScheduledRuns();
    } catch (error) {
      console.error("Error toggling scheduled run:", error);
      alert("Error toggling scheduled run");
    }
  };

  return (
    <div className="modal fade" id="scheduled-run-modal">
      <div className="modal-dialog modal-lg modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">
              <i className="bi bi-clock-fill me-2"></i>Scheduled Runs
            </h5>
          </div>
          <div className="modal-body">
            <div className="row">
              {/* Left Column - Create/Edit Form */}
              <div className="col-md-5">
                <div className="card scheduled-run-card">
                  <div className="card-header">
                    <h6 className="mb-0">
                      <i className="bi bi-plus-circle me-2"></i>
                      {isEditing ? "Edit Schedule" : "Create New Schedule"}
                    </h6>
                  </div>
                  <div className="card-body">
                    <div className="d-flex flex-column gap-3">
                      {/* Schedule Name */}
                      <div>
                        <label className="form-label fw-bold">
                          Schedule Name
                        </label>
                        <input
                          className="form-control"
                          type="text"
                          placeholder="Enter schedule name"
                          value={scheduledRun.name}
                          onChange={(e) =>
                            handleScheduledRunChange("name", e.target.value)
                          }
                        />
                      </div>

                      {/* Include Tags */}
                      <div>
                        <label className="form-label fw-bold">
                          Include Tags
                        </label>
                        <Select
                          isMulti
                          name="includeTags"
                          closeMenuOnSelect={false}
                          components={makeAnimated()}
                          options={tagOptions}
                          className="basic-multi-select"
                          classNamePrefix="select"
                          onChange={(selected) => {
                            handleScheduledRunChange(
                              "includeTags",
                              selected?.map((opt) => opt.value) || []
                            );
                          }}
                        />
                      </div>

                      {/* Exclude Tags */}
                      <div>
                        <label className="form-label fw-bold">
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
                            handleScheduledRunChange(
                              "excludeTags",
                              selected?.map((opt) => opt.value) || []
                            );
                          }}
                        />
                      </div>

                      {/* Schedule Type */}
                      <div>
                        <label className="form-label fw-bold">
                          Schedule Type
                        </label>
                        <Select
                          name="scheduleType"
                          options={scheduleTypes}
                          className="basic-multi-select"
                          classNamePrefix="select"
                          onChange={(selected) => {
                            handleScheduledRunChange(
                              "scheduleType",
                              selected?.value || ""
                            );
                          }}
                        />
                      </div>

                      {/* Time Input */}
                      {scheduledRun.scheduleType !== "custom" && (
                        <div>
                          <label className="form-label fw-bold">Time</label>
                          <input
                            className="form-control"
                            type="time"
                            value={scheduledRun.time}
                            onChange={(e) =>
                              handleScheduledRunChange("time", e.target.value)
                            }
                          />
                          {/* <TimePicker value={scheduledRun.time} onChange={(selected) => {
                            handleScheduledRunChange(
                              "time",
                              selected?.value || ""
                            );
                          }} /> */}
                        </div>
                      )}

                      {/* Day of Week for Weekly */}
                      {scheduledRun.scheduleType === "weekly" && (
                        <div>
                          <label className="form-label fw-bold">
                            Day of Week
                          </label>
                          <Select
                            name="scheduleType"
                            options={daysOfWeek}
                            className="basic-multi-select"
                            classNamePrefix="select"
                            onChange={(selected) => {
                              handleScheduledRunChange(
                                "dayOfWeek",
                                selected?.value || ""
                              );
                            }}
                          />
                        </div>
                      )}

                      {/* Custom Cron Expression */}
                      {scheduledRun.scheduleType === "custom" && (
                        <div>
                          <label className="form-label fw-bold">
                            Cron Expression
                          </label>
                          <input
                            className="form-control"
                            type="text"
                            placeholder="0 0 9 * * ?"
                            value={scheduledRun.cronExpression}
                            onChange={(e) =>
                              handleScheduledRunChange(
                                "cronExpression",
                                e.target.value
                              )
                            }
                          />
                        </div>
                      )}

                      {/* Buttons */}
                      <div className="d-flex gap-2">
                        <button
                          type="button"
                          className="btn btn-primary w-100"
                          onClick={scheduledRunSubmit}
                          disabled={
                            !scheduledRun.name ||
                            (scheduledRun.includeTags.length === 0 &&
                              scheduledRun.excludeTags.length === 0) ||
                            (scheduledRun.scheduleType === "custom" &&
                              !scheduledRun.cronExpression)
                          }
                        >
                          <i
                            className={`bi ${
                              isEditing ? "bi-check-lg" : "bi-plus-lg"
                            } me-1`}
                          ></i>
                          {isEditing ? "Update" : "Create"}
                        </button>
                        {isEditing && (
                          <button
                            type="button"
                            className="btn btn-secondary w-100"
                            onClick={resetScheduledRunForm}
                          >
                            Cancel
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Right Column - Existing Scheduled Runs */}
              <div className="col-md-7">
                <div className="card scheduled-run-card">
                  <div className="card-header d-flex justify-content-between align-items-center">
                    <h6 className="mb-0">
                      <i className="bi bi-list-ul me-2"></i>
                      Existing Schedules ({scheduledRuns.length})
                    </h6>
                    <button
                      type="button"
                      className="btn btn-sm btn-outline-primary"
                      onClick={loadScheduledRuns}
                    >
                      <i className="bi bi-arrow-clockwise"></i>
                    </button>
                  </div>
                  <div
                    className="card-body"
                    style={{ maxHeight: "450px", overflowY: "auto" }}
                  >
                    {scheduledRuns.length === 0 ? (
                      <div className="text-center p-4 text-muted">
                        <i className="bi bi-calendar-x fs-1"></i>
                        <p className="mt-2">No scheduled runs found</p>
                      </div>
                    ) : (
                      <div className="col d-flex flex-column gap-3">
                        {scheduledRuns.map((run) => (
                          <div key={run.id} className="row">
                            <div className="d-flex justify-content-between align-items-start">
                              <div className="flex-grow-1">
                                <div className="d-flex align-items-center mb-2">
                                  <h6 className="mb-0 me-2 fw-bold">{run.name}</h6>
                                  <span
                                    className={`badge ${
                                      run.active ? "bg-success" : "bg-secondary"
                                    }`}
                                  >
                                    {run.active ? "Active" : "Disabled"}
                                  </span>
                                </div>
                                <p className="mb-0 small text-muted">
                                  <i className="bi bi-clock me-1"></i>
                                  {parsedCron(run.cronExpression)}
                                </p>
                                {run.includeTags &&
                                  run.includeTags.length > 0 && (
                                    <p className="mb-1 small">
                                      <span className="fw-bold text-success">
                                        Include:
                                      </span>{" "}
                                      {run.includeTags.join(", ")}
                                    </p>
                                  )}
                                {run.excludeTags &&
                                  run.excludeTags.length > 0 && (
                                    <p className="mb-1 small">
                                      <span className="fw-bold text-danger">
                                        Exclude:
                                      </span>{" "}
                                      {run.excludeTags.join(", ")}
                                    </p>
                                  )}
                                {run.lastRunAt && (
                                  <p className="mb-0 small text-muted">
                                    Last run:{" "}
                                    {new Date(run.lastRunAt).toLocaleString()}
                                  </p>
                                )}
                              </div>
                              <div className="d-flex flex-column gap-1">
                                <div
                                  className="btn-group-vertical"
                                  role="group"
                                >
                                  <button
                                    className="btn btn-sm btn-outline-primary"
                                    onClick={() => editScheduledRun(run)}
                                    title="Edit"
                                  >
                                    <i className="bi bi-pencil"></i>
                                  </button>
                                  <button
                                    className={`btn btn-sm btn-outline-${
                                      run.active ? "warning" : "success"
                                    }`}
                                    onClick={() =>
                                      toggleScheduledRun(run.id, run.active)
                                    }
                                    title={run.active ? "Disable" : "Enable"}
                                  >
                                    <i
                                      className={`bi ${
                                        run.active ? "bi-pause" : "bi-play"
                                      }`}
                                    ></i>
                                  </button>
                                  <button
                                    className="btn btn-sm btn-outline-danger"
                                    onClick={() => deleteScheduledRun(run.id)}
                                    title="Delete"
                                  >
                                    <i className="bi bi-trash"></i>
                                  </button>
                                </div>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-secondary"
              data-bs-dismiss="modal"
              onClick={resetScheduledRunForm}
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ScheduledRun;
