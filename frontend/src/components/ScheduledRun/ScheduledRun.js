import { useEffect, useState } from "react";
import Select from "react-select";
import makeAnimated from "react-select/animated";
import "./ScheduledRun.css";
import parsedCron from "../utils/CronParser";
import { FaEllipsisV } from "react-icons/fa";

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
    },
  ];
  const [scheduledRun, setScheduledRun] = useState({
    name: "",
    includeTags: [],
    excludeTags: [],
    scheduleType: "daily", // daily, weekly, custom
    time: "09:00",
    dayOfWeek: "1", // 1=Monday, 7=Sunday
    cronExpression: "0 0 9 * * ?",
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

  const handleCollapseForm = (collapse) => {
    const collapseForm = document.getElementById("accordion-box");
    if (collapseForm && collapse) {
      collapseForm.classList.remove("show");
    } else if (collapseForm && !collapse) {
      collapseForm.classList.add("show");
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
      cronExpression: "0 0 9 * * ?",
    });
    setIsEditing(false);
    setEditingId(null);
  };

  // Load scheduled runs when modal opens
  useEffect(() => {
    const modalElement = document.getElementById("scheduled-run-modal");
    const listener = () => handleCollapseForm(true);
    if (modalElement) {
      modalElement.addEventListener("shown.bs.modal", loadScheduledRuns);
      modalElement.addEventListener("hidden.bs.modal", listener);
      modalElement.addEventListener("hidden.bs.modal", resetScheduledRunForm);
    }

    return () => {
      modalElement.removeEventListener("shown.bs.modal", loadScheduledRuns);
      modalElement.removeEventListener("hidden.bs.modal", listener);
      modalElement.removeEventListener(
        "hidden.bs.modal",
        resetScheduledRunForm
      );
    };
  }, []);

  const scheduledRunSubmit = async () => {
    try {
      const cronExpression = generateCronExpression();
      const payload = {
        ...scheduledRun,
        includeTags: scheduledRun.includeTags.map((tag) => tag.value),
        excludeTags: scheduledRun.excludeTags.map((tag) => tag.value),
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

  const editScheduledRun = (run) => {
    setScheduledRun({
      name: run.name,
      includeTags:
        run.includeTags.map((tag) => ({ value: tag, label: tag })) || [],
      excludeTags:
        run.excludeTags.map((tag) => ({ value: tag, label: tag })) || [],
      scheduleType: "custom",
      time: "09:00",
      dayOfWeek: "1",
      cronExpression: run.cronExpression,
    });
    setIsEditing(true);
    setEditingId(run.id);
    handleCollapseForm(false);
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
              <div className="col d-flex flex-column gap-3">
                <div className="accordion-custom">
                  <button
                    type="button"
                    className="btn glass-button accor-btn"
                    data-bs-toggle="collapse"
                    data-bs-target="#accordion-box"
                  >
                    {isEditing ? "Edit Schedule" : "Create New Schedule"}
                  </button>
                  <div id="accordion-box" className="collapse accordion-box">
                    <div className="d-flex flex-column gap-3">
                      <div>
                        <label className="form-label fw-bold">
                          Schedule Name
                        </label>
                        <input
                          className="form-control"
                          type="text"
                          placeholder="Enter Schedule Name"
                          value={scheduledRun.name}
                          onChange={(e) =>
                            handleScheduledRunChange("name", e.target.value)
                          }
                        />
                      </div>
                      <div className="d-flex flex-row gap-4">
                        <div className="col">
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
                            value={scheduledRun.includeTags}
                            onChange={(selected) => {
                              handleScheduledRunChange(
                                "includeTags",
                                selected || []
                              );
                            }}
                          />
                        </div>
                        <div className="col">
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
                            value={scheduledRun.excludeTags}
                            onChange={(selected) => {
                              handleScheduledRunChange(
                                "excludeTags",
                                selected || []
                              );
                            }}
                          />
                        </div>
                      </div>

                      <div className="d-flex flex-row gap-4">
                        <div className="col">
                          <label className="form-label fw-bold">
                            Schedule Type
                          </label>
                          <Select
                            name="scheduleType"
                            options={scheduleTypes}
                            defaultValue={scheduleTypes[0]}
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

                        {scheduledRun.scheduleType !== "custom" && (
                          <div className="col">
                            <label className="form-label fw-bold">Time</label>
                            <input
                              className="form-control"
                              type="time"
                              value={scheduledRun.time}
                              onChange={(e) =>
                                handleScheduledRunChange("time", e.target.value)
                              }
                            />
                          </div>
                        )}

                        {scheduledRun.scheduleType === "weekly" && (
                          <div className="col">
                            <label className="form-label fw-bold">
                              Day of Week
                            </label>
                            <Select
                              name="scheduleType"
                              options={daysOfWeek}
                              defaultValue={daysOfWeek[0]}
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

                        {scheduledRun.scheduleType === "custom" && (
                          <div className="col">
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
                      </div>
                      <div className="d-flex gap-4">
                        {isEditing ? (
                          <button
                            type="button"
                            className="btn glass-button success-btn w-100"
                            onClick={scheduledRunSubmit}
                            disabled={
                              !scheduledRun.name ||
                              (scheduledRun.includeTags.length === 0 &&
                                scheduledRun.excludeTags.length === 0) ||
                              (scheduledRun.scheduleType === "custom" &&
                                !scheduledRun.cronExpression)
                            }
                          >
                            Update
                          </button>
                        ) : (
                          <button
                            type="button"
                            className="btn glass-button primary-btn w-100"
                            onClick={scheduledRunSubmit}
                            disabled={
                              !scheduledRun.name ||
                              (scheduledRun.includeTags.length === 0 &&
                                scheduledRun.excludeTags.length === 0) ||
                              (scheduledRun.scheduleType === "custom" &&
                                !scheduledRun.cronExpression)
                            }
                          >
                            Create
                          </button>
                        )}

                        {isEditing && (
                          <button
                            type="button"
                            className="btn glass-button delete-btn w-100"
                            onClick={resetScheduledRunForm}
                          >
                            Cancel
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
                <div className="d-flex justify-content-between align-items-center">
                  <h6 className="mb-0">
                    <i className="bi bi-list-ul me-2"></i>
                    Existing Schedules ({scheduledRuns.length})
                  </h6>
                  <button
                    type="button"
                    className="btn glass-button primary-btn btn-sm"
                    onClick={loadScheduledRuns}
                  >
                    <i className="bi bi-arrow-clockwise"></i>
                  </button>
                </div>
                <div className="schedule-runs d-flex flex-column gap-4">
                  {scheduledRuns.map((run, key) => (
                    <div key={run.id} className="card scheduled-run-card">
                      <div className="card-body ms-1 d-flex flex-column gap-2 pb-0">
                        {scheduledRuns.length === 0 ? (
                          <div className="text-center p-4 text-muted">
                            <i className="bi bi-calendar-x fs-1"></i>
                            <p className="mt-2">No scheduled runs found</p>
                          </div>
                        ) : (
                          <div>
                            <div className="schedule-title d-flex justify-content-between">
                              <div className="d-flex align-items-center mb-1">
                                <h6 className="fw-bold mb-0 me-2">
                                  {run.name}
                                </h6>
                                <span
                                  className={`badge ${
                                    run.active ? "bg-success" : "bg-secondary"
                                  }`}
                                >
                                  {run.active ? "Active" : "Disabled"}
                                </span>
                              </div>
                              <div className="d-flex gap-1 action-buttons">
                                <div
                                  className="collapse"
                                  id={`action-btns-${key}`}
                                  role="group"
                                >
                                  <button
                                    className="btn btn-sm glass-button primary-btn-dark me-1"
                                    onClick={() => editScheduledRun(run)}
                                    title="Edit"
                                  >
                                    <i className="bi bi-pencil"></i>
                                  </button>
                                  <button
                                    className={`btn btn-sm glass-button ${
                                      run.active ? "warning" : "success"
                                    }-btn-dark me-1`}
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
                                    className="btn btn-sm glass-button delete-btn-dark"
                                    onClick={() => deleteScheduledRun(run.id)}
                                    title="Delete"
                                  >
                                    <i className="bi bi-trash"></i>
                                  </button>
                                </div>
                                <button
                                  className="btn btn-sm glass-button"
                                  key={key}
                                  title="Toggle Actions"
                                  data-bs-target={`#action-btns-${key}`}
                                  data-bs-toggle="collapse"
                                  aria-expanded="false"
                                  aria-controls={`action-btns-${key}`}
                                >
                                  <FaEllipsisV />
                                </button>
                              </div>
                            </div>
                            <div className="schedule-info">
                              <p className="mb-0 small text-muted">
                                <i className="bi bi-clock me-1"></i>
                                {parsedCron(run.cronExpression)}
                              </p>
                              {run.includeTags &&
                                run.includeTags.length > 0 && (
                                  <p
                                    className={`${
                                      run.lastRunAt ||
                                      (run.excludeTags &&
                                        run.excludeTags.length > 0)
                                        ? "mb-0"
                                        : ""
                                    } small`}
                                  >
                                    <span className="fw-bold text-success">
                                      Include:
                                    </span>{" "}
                                    {run.includeTags.join(", ")}
                                  </p>
                                )}

                              {run.excludeTags &&
                                run.excludeTags.length > 0 && (
                                  <p
                                    className={`${
                                      run.lastRunAt ? "mb-0" : ""
                                    } small`}
                                  >
                                    <span className="fw-bold text-danger">
                                      Exclude:
                                    </span>{" "}
                                    {run.excludeTags.join(", ")}
                                  </p>
                                )}

                              {run.lastRunAt && (
                                <p className="small text-muted">
                                  Last run:{" "}
                                  {new Date(run.lastRunAt).toLocaleString()}
                                </p>
                              )}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button
              type="button"
              className="btn glass-button"
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
