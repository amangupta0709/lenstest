import Select from "react-select";
import makeAnimated from "react-select/animated";
import React, { useEffect, useState } from "react";

const NewRun = ({ tagOptions }) => {
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

  const resetNewRun = () => {
    setNewRun({
      includeTags: [],
      excludeTags: [],
    });
  };

  useEffect(() => {
    const modalElement = document.getElementById("new-run-modal");
    if (modalElement) {
      modalElement.addEventListener("show.bs.modal", resetNewRun);
      return () =>
        modalElement.removeEventListener("show.bs.modal", resetNewRun);
    }
  }, []);

  const newRunSubmit = async () => {
    try {
      const transformedRun = {
        includeTags: newRun.includeTags.map((tag) => tag.value),
        excludeTags: newRun.excludeTags.map((tag) => tag.value),
      };
      const url = "http://localhost:8080/api/tests/execute";
      await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(transformedRun),
      });
    } catch (error) {
      console.error("Fetch error:", error);
    }
  };
  return (
    <div className="modal fade" id="new-run-modal">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">
              <i className="bi bi-gear-fill me-2"></i>New Run
            </h5>
          </div>
          <div className="modal-body">
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
                  className="select-container"
                  classNamePrefix="select"
                  value={newRun.includeTags}
                  onChange={(selected) => {
                    handleNewRunChange("includeTags", selected || []);
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
                  value={newRun.excludeTags}
                  classNamePrefix="select"
                  className="select-container"
                  onChange={(selected) => {
                    handleNewRunChange("excludeTags", selected || []);
                  }}
                />
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button
              type="button"
              className="btn glass-button primary-btn"
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
  );
};

export default NewRun;
