
import React, { useMemo } from 'react';
import { Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS } from 'chart.js/auto';

const ReportDetails = (build) => {

    console.log("ReportDetails", build);

    const toggleSummary = () => {
        const summary = document.querySelector('.summary');
        if (summary) {
            summary.classList.toggle('d-none');
        }
    }

    const data = {
        labels: [
            'Passed',
            'Failed',
            'Skipped'
        ],
        datasets: [{
            // data: [300, 50, 100],
            backgroundColor: [
                'rgb(83, 221, 28)', 
                'rgb(231, 12, 12)', 
                'rgb(8, 98, 244)'
            ],
            borderColor: 'transparent',
            hoverOffset: 8
        }]
    };
    const chartOptions = {
        maintainAspectRatio: false,
        cutout: '70%',
        animation: {
            duration: 2000
        },
        label:'ggggg',
        plugins: {
            legend: {
                position: 'right',
                labels: {
                    boxWidth: 15
                }
            }
        }
    }

  return (
    <div className="main-content app-content">
        <div class="page-header mt-4 pt-4">
            <div class="container">
                <div class="d-flex justify-content-between">
                    <h4>Build #{build.id}</h4>
                    <div>
                    <button type="button" class="btn btn-outline-primary btn-sm ms-1" onClick={toggleSummary}>
                        <i class="bi bi-bar-chart-fill me-1"></i> Toggle Summary
                    </button>
                    </div>
                </div>
            </div>
        </div>
        <div class="container-fluid bg-body-tertiary">
            <div class="container pt-4 pb-3 summary">
                <div class="row">
                    {Object.entries(build.statsSummaryJson || {}).map(item => (
                        <div className="col-4">
                            <div className="card" style={{ height: '200px' }}>
                                <div className="card-header pt-3 pb-3">
                                    <h6 className="mb-0">{item[0]}</h6>
                                </div>
                                <div className="card-body py-0">
                                    <div className="d-flex justify-content-center" style={{ height: '80px' }}>
                                        {/* <canvas baseChart></canvas> */}
                                        <Doughnut data={{...data,datasets:[{...data.datasets[0],data:[item[1].passed,item[1].failed,item[1].skipped]}]}} options={chartOptions} />
                                    </div>
                                </div>
                                <div className="card-footer smaller">
                                    {item[1].passed} Passed,
                                    {item[1].failed} Failed,
                                    {item[1].skipped} Skipped
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    </div>
  );
}

export default ReportDetails;