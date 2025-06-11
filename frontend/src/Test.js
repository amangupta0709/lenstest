

const Test = () => {
  return (
    <nav class="navbar border-bottom">
    <div class="container d-flex justify-content-between">
      <div>
        <a class="navbar-brand" href="#">ChainTest</a>
        <span class="ms-3 fs-6">LensTest</span>
      </div>
      <div>
        <span class="badge badge-outline text-lg"><i class="bi bi-hourglass me-1"></i> a</span>
        <span class="badge badge-outline text-lg"><i class="bi bi-clock me-1 text-success"></i> b</span>
        <span class="badge badge-outline text-lg me-3"><i class="bi bi-clock me-1 text-danger"></i> c</span>
        <button role="button" id="shortcuts" class="btn btn-outline-primary smaller" title="Shortcuts">
          <i class="bi bi-info-circle"></i></button>
      </div>
    </div>
  </nav>
  );
}

export default Test;
