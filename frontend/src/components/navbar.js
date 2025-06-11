import logo from './lenstest-logo-dark.png';
import Navbar from 'react-bootstrap/Navbar';
import Container from 'react-bootstrap/Container';

const Nav = (data) => {
    // console.log(data);
  return (
    <nav className="navbar border-bottom">
    <div className="container d-flex justify-content-between">
      <div>
        <a className="navbar-brand" href="#">
            <img src={logo} alt="LensTest" width="150" height="50"></img>
        </a>
        <span className="ms-3 fs-6"></span>
      </div>
      <div>
        {/* <span className="badge badge-outline text-lg"><i className="bi bi-hourglass me-1"></i>{data.props['durationPretty']}</span> */}
        {/* <span className="badge badge-outline text-lg"><i className="bi bi-clock me-1 text-success"></i> b</span>
        <span className="badge badge-outline text-lg me-3"><i className="bi bi-clock me-1 text-danger"></i> c</span> */}
        <button role="button" id="shortcuts" className="btn btn-outline-primary smaller" title="Shortcuts">
          <i className="bi bi-info-circle"></i></button>
      </div>
    </div>
  </nav>
  );
}

export default Nav;