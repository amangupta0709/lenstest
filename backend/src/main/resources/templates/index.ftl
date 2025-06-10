<#assign
    title1 = 'Features', title2 = 'Scenarios', title3 = 'Steps', statLevel = 1
>
<#if build.runStats?? && build.runStats?has_content && (statLevel gte build.runStats?size) && build.runStats?size gte 1>
    <#assign statLevel = build.runStats?size - 1>
</#if>

<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8">
  <meta name='viewport' content='width=device-width, initial-scale=1.0'>
  <title>LensTest Report</title>
  <link id="style" href="resources/static/lenstest.css" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
</head>

<body data-bs-theme="dark">
  <nav class="navbar border-bottom">
    <div class="container d-flex justify-content-between">
      <div>
        <a class="navbar-brand" href="#">ChainTest</a>
        <span class="ms-3 fs-6">LensTest</span>
      </div>
      <div>
        <span class="badge badge-outline text-lg"><i class="bi bi-hourglass me-1"></i> ${build.durationPretty}</span>
        <span class="badge badge-outline text-lg"><i class="bi bi-clock me-1 text-success"></i> ${build.startedAt?number_to_datetime?string('yyyy-MM-dd hh:mm:ss a')}</span>
        <span class="badge badge-outline text-lg me-3"><i class="bi bi-clock me-1 text-danger"></i> ${build.endedAt?number_to_datetime?string('yyyy-MM-dd hh:mm:ss a')}</span>
        <button role="button" id="shortcuts" class="btn btn-outline-primary smaller" title="Shortcuts">
          <i class="bi bi-info-circle"></i></button>
      </div>
    </div>
  </nav>

  <div id="summary" class="container-fluid bg-body-tertiary">
    <!-- dashboard section -->
    <#if build.runStats?? && build.runStats?size !=0>
    <div id="dashboard" class="py-5">
      <div class="container">
        <div class="row">
          <div class="col-4">
            <div class="card card-custom" style="height: 175px">
              <div class="card-header">
                ${title1}
              </div>
              <div class="card-body d-flex justify-content-center" style="height: 90px;">
                <div class="chart-view center" style="margin-left: -1rem;">
                  <canvas id="stats1"></canvas>
                </div>
              </div>
              <div class="card-footer small">
                <span id="sp1">${build.runStats[0].passed}</span> Passed,
                <span id="sf1">${build.runStats[0].failed}</span> Failed,
                <span id="ss1">${build.runStats[0].skipped}</span> Skipped
              </div>
            </div>
          </div>
          <#if build.runStats?size gte 2>
          <div class="col-4">
            <div class="card card-custom" style="height: 175px">
              <div class="card-header">
                ${title2}
              </div>
              <div class="card-body d-flex justify-content-center" style="height: 90px;">
                <div class="chart-view center" style="margin-left: -1rem;">
                  <canvas id="stats2"></canvas>
                </div>
              </div>
              <div class="card-footer small">
                <span id="sp2">${build.runStats[1].passed}</span> Passed,
                <span id="sf2">${build.runStats[1].failed}</span> Failed,
                <span id="ss2">${build.runStats[1].skipped}</span> Skipped
              </div>
            </div>
          </div>
          </#if>
          <#if build.runStats?size gte 3>
            <div class="col-4">
              <div class="card card-custom" style="height: 175px">
                <div class="card-header">
                  ${title3}
                </div>
                <div class="card-body d-flex justify-content-center" style="height: 90px;">
                  <div class="chart-view center" style="margin-left: -1rem;">
                    <canvas id="stats3"></canvas>
                  </div>
                </div>
                <div class="card-footer small">
                  <span id="sp3">${build.runStats[2].passed}</span> Passed,
                  <span id="sf3">${build.runStats[2].failed}</span> Failed,
                  <span id="ss3">${build.runStats[2].skipped}</span> Skipped
                </div>
              </div>
            </div>
          </#if>
        </div>
      </div>
    </div>
    </#if>
    <!-- /dashboard section -->

    <!-- tag section -->
    <#if build.tags?has_content>
      <div id="tags" class="container pb-5">
        <div class="card card-custom">
          <div class="card-header">
            Tags
          </div>
          <div class="card-body">
            <table id="tag-summary" class="table">
              <thead>
                <tr>
                  <th scope="col" style="width:65%"></th>
                  <th scope="col">Total</th>
                  <th scope="col">Passed</th>
                  <th scope="col">Failed</th>
                  <th scope="col">Time</th>
                </tr>
              </thead>
              <tbody>
                <#list build.tagStats as tag>
                  <#if tag.depth == statLevel>
                  <tr>
                    <td><a href="#" class="secondary tag">${tag.name}</a></td>
                    <td>${tag.total}</td>
                    <td>${tag.passed}</td>
                    <td>${tag.failed}</td>
                    <td>${tag.durationPretty}</td>
                  </tr>
                  </#if>
                </#list>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div class="border-bottom"></div>
    </#if>
    <!-- /tag section -->
  </div>

  <div class="container-fluid">
    <div class="py-3 mb-5">
      <div class="container d-flex justify-content-between">
        <div class="input-group">
          <input type="text" id="q" class="form-control border" placeholder="Search..." aria-label="Search...">
          <button class="btn border btn-sm text-success status-filter" id="passed" type="button">Passed</button>
          <button class="btn border btn-sm text-warning status-filter" id="skipped" type="button">Skipped</button>
          <button class="btn border btn-sm text-danger status-filter" id="failed" type="button">Failed</button>
          <button id="clear-filters" class="btn border btn-sm"><i class="bi bi-x-lg me-1"></i>Clear all filters</button>
          <button type="button" id="summary-toggle" class="btn border btn-sm"><i class="bi bi-toggle-on me-1"></i> Toggle Summary Section</button>
        </div>
      </div>
    </div>
    <#list tests as test>
      <div class="test-container ${test.result?lower_case}">
        <div class="container">
          <div class="row">
            <div class="col-4">
              <h6 class="mb-3 testname fs-6 ${test.result?lower_case}">${test.name}</h6>
              <div class="small">
                <span class="badge badge-outline"><i class="bi bi-hourglass me-1"></i> ${test.durationPretty}</span>
                <span class="badge text-bg-info"><i class="bi bi-clock me-1"></i>
                  ${test.startedAt?number_to_datetime?string(config['datetimeFormat'])}</span>
                <span class="badge text-bg-warning"><i class="bi bi-clock me-1"></i>
                  ${test.endedAt?number_to_datetime?string(config['datetimeFormat'])}</span>
              </div>
              <#if test.tags?has_content>
                <div class="my-2">
                  <#list test.tags as tag>
                    <span class="badge rounded-pill text-bg-secondary">${tag.name}</span>
                  </#list>
                </div>
              </#if>
              <#if test.description??><code class="mt-2">${test.description}</code></#if>
            </div>
            <div class="col-8">
              <#list test.children as child>
                <div class="card mb-3 result ${child.result?lower_case}">
                  <div class="card-body">
                    <div class="d-flex justify-content-between">
                      <div>
                        <#if child.result=='PASSED'>
                          <i class="bi bi-check-circle-fill text-success"></i>
                        <#elseif child.result=='SKIPPED'>
                          <i class="bi bi-exclamation-octagon-fill text-info"></i>
                        <#else>
                          <i class="bi bi-exclamation-octagon-fill text-danger"></i>
                        </#if>
                        <span class="fs-6 ms-2">${child.name}</span>
                      </div>
                      <div>
                        <#if child.tags?has_content>
                          <span class="tag-list">
                            <#list child.tags as tag>
                              <span class="badge rounded-pill text-bg-secondary">${tag.name}</span>
                            </#list>
                          </span>
                          <span class="mx-1">&middot;</span>
                        </#if>
                        <span class="badge text-dark bg-light">${child.durationPretty}</span>
                      </div>
                    </div>
                    <#if child.description??><code class="ms-4 small">${child.description}</code></#if>
                    <#if child.error??>
                      <pre class="mt-2">${child.error}</pre>
                    </#if>
                    <#include "bdd.ftl" />
                    <#if child.logs?has_content>
                      <div class="mt-3">
                        <pre class="pb-0">
                          <#list child.logs as log>
${log.message}
                          </#list>
                        </pre>
                      </div>
                    </#if>
                  </div>
                </div>
              </#list>
            </div>
          </div>
        </div>
        <#if test?is_last>
          <div class="border-bottom py-5"></div>
          <#else>
            <div class="border-bottom my-5"></div>
        </#if>
      </div>
    </#list>
  </div>

  <#if build.runStats?? && build.runStats?size != 0>
  <script>
    const stats1Annotation = {'total': ${ build.runStats[0].total }, 'passed': ${ build.runStats[0].passed }};
    const stats1 = [
      { result: 'Passed', count: ${ build.runStats[0].passed }, bg: 'rgb(140, 197, 83)' },
      { result: 'Failed', count: ${ build.runStats[0].failed }, bg: 'rgb(233,80,113)' },
      { result: 'Skipped', count: ${ build.runStats[0].skipped }, bg: 'rgb(13, 202, 240)' }
    ];
    <#if build.runStats?size gte 2>
      const stats2Annotation = {'total': ${ build.runStats[1].total }, 'passed': ${ build.runStats[1].passed }};
      const stats2 = [
        { result: 'Passed', count: ${ build.runStats[1].passed }, bg: 'rgb(140, 197, 83)' },
        { result: 'Failed', count: ${ build.runStats[1].failed }, bg: 'rgb(233,80,113)' },
        { result: 'Skipped', count: ${ build.runStats[1].skipped }, bg: 'rgb(13, 202, 240)' }
      ];
    <#else>
      const stats2 = null;
    </#if>
    <#if build.runStats?size gte 3>
      const stats3Annotation = {'total': ${ build.runStats[2].total }, 'passed': ${ build.runStats[2].passed }};
      const stats3 = [
        { result: 'Passed', count: ${ build.runStats[2].passed }, bg: 'rgb(140, 197, 83)' },
        { result: 'Failed', count: ${ build.runStats[2].failed }, bg: 'rgb(233,80,113)' },
        { result: 'Skipped', count: ${ build.runStats[2].skipped }, bg: 'rgb(13, 202, 240)' }
      ];
    <#else>
      const stats3 = null;
    </#if>
  </script>
  <#else>
    const stats1 = null;
  </#if>
  <script src="resources/static/lenstest.js"></script>

  <div id="info-modal" class="modal" tabindex="-1">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h6 class="modal-title">ChainTest Simple Generator - Shortcuts</h6>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"
            onclick="toggleInfoModal(false)"></button>
        </div>
        <div class="modal-body">
          <table class="table">
            <tbody>
              <#list shortcuts?keys as k>
                <tr>
                  <td>${k}</td>
                  <td>${shortcuts[k]}</td>
                </tr>
              </#list>
            </tbody>
          </table>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal"
            onclick="toggleInfoModal(false)">Close</button>
        </div>
      </div>
    </div>
  </div>

  <div id="attachment-modal" class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h6 class="modal-title">Attachment</h6>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"
            onclick="toggleAttachmentModal(false)"></button>
        </div>
        <div class="modal-body">
          <img src="" />
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal"
            onclick="toggleAttachmentModal(false)">Close</button>
        </div>
      </div>
    </div>
  </div>

</body>

</html>