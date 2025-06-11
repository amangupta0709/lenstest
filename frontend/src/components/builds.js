const Builds = (data) => {

    // const config = {
    //     type: 'line',
    //     data: data,
    // };
    // const labels = Utils.months({count: 7});
    // const data = {
    //     labels: labels,
    //     datasets: [{
    //         label: 'My First Dataset',
    //         data: [65, 59, 80, 81, 56, 55, 40],
    //         fill: false,
    //         borderColor: 'rgb(75, 192, 192)',
    //         tension: 0.1
    //     }]
    // };
    console.log(data);
    return (
        <div className="main-content app-content">
            <div className="page-header mt-4 pt-4">
                <div className="container">
                    <div className="d-flex justify-content-between">
                        <h2>Builds</h2>
                    </div>
                </div>
            </div>
            <div className="container mt-4">
                <div className="row">
                    <div className="col-8">
                        {/* <h2>Lens Builds</h2> */}
                        <div className="mt-4 mb-3">
                            <div class="accordion">
                                <div class="accordion-item">
                                    <h2 class="accordion-header" id="headingOne">
                                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
                                            <i className="bi bi-search me-3"></i>
                                            Filter
                                        </button>
                                    </h2>
                                    <div id="collapseOne" class="accordion-collapse collapse" aria-labelledby="headingOne" data-bs-parent="#accordionExample">
                                        <div class="accordion-body">
                                            <div class="row mb-3">
                                                <div className="col-4"> 
                                                    Status 
                                                </div>
                                                <div className="col-8">
                                                    <div class="form-check form-check-inline">
                                                        <input class="form-check-input" type="radio" name="inlineRadioOptions" id="inlineRadio1" value="Passed"/>
                                                        <label class="form-check-label" for="inlineRadio1">Passed</label>
                                                    </div>
                                                    <div class="form-check form-check-inline">
                                                        <input class="form-check-input" type="radio" name="inlineRadioOptions" id="inlineRadio2" value="Failed"/>
                                                        <label class="form-check-label" for="inlineRadio2">Failed</label>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="row mb-3">
                                                <div className="col-4"> 
                                                    Date Range
                                                </div>
                                                <div className="col-4">
                                                    <label for="startDate" class="form-label">From</label>
                                                    <div class="input-group date" data-provide="datepicker">
                                                        {/* <span class="input-group-text">
                                                        <i className="bi bi-calendar"></i>
                                                    </span> */}
                                                        <input type="date" class="form-control ps-2" id="startDate" name="startDate" placeholder="From"></input>
                                                    </div>
                                                </div>
                                                 <div className="col-4">
                                                    <label for="startDate" class="form-label">To</label>
                                                    <div class="input-group date" data-provide="datepicker">
                                                        {/* <span class="input-group-text">
                                                        <i className="bi bi-calendar"></i>
                                                    </span> */}
                                                        <input type="date" class="form-control ps-2" id="endDate" name="endDate" placeholder="To"></input>
                                                    </div>
                                                </div>
                                            </div>
                                            <button type="button" id="searchBtn" class="btn btn-primary me-2">Search</button>
                                            <button type="button" id="resetBtn" class="btn btn-secondary">Reset</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        { data.props.map(item => (
                            <div class="card">
                                <div class="card-body d-on-hover">
                                    <div class="ms-1 mb-2 d-flex justify-content-between">
                                        <div class="build-name pointer">
                                            { item.executionStage === 'FINISHED' ?
                                                <i className="bi bi-check-circle-fill text-success"></i>
                                            :
                                                <span class="spinner-border spinner-border-sm text-primary"></span>
                                            }
                                            <span className="ms-2 fw-semibold">Build #{item.id}</span>
                                        </div>
                                    </div>
                                    <div class="ms-1">
                                        <div className="text-secondary">
                                            <span className="me-3 small">
                                                <i className="bi bi-hourglass me-2"></i>
                                                {item.durationPretty}
                                            </span>
                                            <br></br>
                                            <span className="small">
                                                <i className="bi bi-calendar me-2"></i>
                                                {item.startedAt}
                                            </span>
                                            - 
                                            <span className="small">
                                                {item.completedAt}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div class="card-footer">
                                    { item.tags.map(tag => (
                                        <span className="badge bg-outline-light me-1 ms-1">{tag}</span>
                                    ))}
                                </div>
                            </div>
                        ))}
                        
                    </div>
                    <div className="col-4">
                        <div className="sticky">
                            <div className="card">
                                <div className="card-header">
                                    <h5 className="mb-0">Build Summary</h5>
                                </div>
                                <div className="card-body py-0">
                                    <div className="mx-auto" style={{ width: '150px', maxHeight: '150px' }}>
                                        {/* Chart.js or any other chart library can be used here */}
                                        {/* Example using Chart.js */}
                                        {/* <canvas id="myChart"></canvas> */}
                                        <canvas basechart width="300" height="300" style={{ display: 'block', boxSizing: "border-box", height: '150px', width: '150px' }}>
                                        </canvas>
                                    </div>
                                </div>
                                <div className="card-footer smaller">
                                    <p>2 passed, 1 failed, 0 skipped</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Builds;