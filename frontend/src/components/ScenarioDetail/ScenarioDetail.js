import { useEffect, useRef, useState } from "react";
import parsedDate from "../utils/DateParser";
import "./ScenarioDetail.css";

const ScenarioDetail = (scenario) => {
  const divRef = useRef(null);
  const [initialHeightSet, setInitialHeightSet] = useState(false);

  useEffect(() => {
    const div = divRef.current;

    if (div && !initialHeightSet) {
      const contentHeight = div.scrollHeight;
      if (contentHeight > 10) {
        div.style.height = "10px";
      } else {
        div.style.height = "auto";
      }
      setInitialHeightSet(true);
    }
  }, [initialHeightSet]);

  return (
    <div className="container d-flex flex-column pb-5">
      <div className="row mt-4">
        <div className="card px-0">
          <div className="card-body">
            <div className="row mx-1">
              <div className="col">
                <h4
                  className={`fw-bold ${
                    scenario.status === "PASSED"
                      ? "success-color"
                      : "failed-color"
                  }`}
                >
                  {scenario.name}
                </h4>
              </div>
              <div className="col d-flex gap-2 justify-content-end m-1">
                <span className="badge scenario-badge me-1 mb-1 align-self-start">
                  {scenario.duration}
                </span>
                <span className="badge scenario-badge me-1 mb-1 align-self-start">
                  {parsedDate(scenario.startedAt)} -{" "}
                  {parsedDate(scenario.completedAt)}
                </span>
              </div>
            </div>
            <div className="mx-3">
              {scenario.tags &&
                scenario.tags.length > 0 &&
                scenario.tags.map((tag) => (
                  <span className="badge badge-dark me-2 mb-1">{tag}</span>
                ))}
            </div>
            <div className="d-flex flex-column gap-4 mt-4">
              {scenario?.steps &&
                scenario.steps.length > 0 &&
                Object.entries(scenario.steps).map(([stepKey, step], index) => (
                  <div className="row box px-2 py-2 mx-2" key={stepKey}>
                    <div
                      className="row pt-2"
                      data-bs-target={`#step-detail-${index}`}
                      data-bs-toggle="collapse"
                      style={{
                        cursor:
                          (step.logs && step.logs.length > 0) || step.error
                            ? "pointer"
                            : "default",
                      }}
                    >
                      <div className="col">
                        <h5
                          className={`step-header ${
                            step.status === "PASSED"
                              ? "success-color"
                              : step.status === "FAILED"
                              ? "failed-color"
                              : "skipped-color"
                          }`}
                        >
                          {step.name}
                        </h5>
                      </div>
                      <div className="col d-flex justify-content-end">
                        <span className="badge scenario-badge me-1 mb-1 align-self-start">
                          {step.duration}
                        </span>
                      </div>
                    </div>
                    {step.dataTable && step.dataTable.length > 0 && (
                      <div className="row">
                        <table className="data-table">
                          <tbody>
                            {step.dataTable.map((row, rowIndex) => (
                              <tr key={rowIndex}>
                                {row.map((cell, colIndex) => (
                                  <td
                                    className="text-center align-middle"
                                    key={colIndex}
                                  >
                                    {cell}
                                  </td>
                                ))}
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                    <div id={`step-detail-${index}`} className="collapse show">
                      {step.logs &&
                        Object.entries(step.logs).filter(
                          ([logKey, log]) => log.showReport
                        ).length > 0 && (
                          <div
                            className="row step-log ms-0 p-3 mt-3 mb-3"
                            ref={divRef}
                            onClick={(e) => e.stopPropagation()}
                          >
                            {Object.entries(step.logs)
                              .filter(([logKey, log]) => log.showReport)
                              .map(([logKey, log]) => (
                                <div className="row">{log.value}</div>
                              ))}
                          </div>
                        )}

                      {step.error && (
                        <div
                          className="row step-error ms-0 p-3 mb-3"
                          ref={divRef}
                          onClick={(e) => e.stopPropagation()}
                        >
                          {step.error}
                        </div>
                      )}
                    </div>
                  </div>
                ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ScenarioDetail;
