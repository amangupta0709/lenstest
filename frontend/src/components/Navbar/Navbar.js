import "./Navbar.css";

const Nav = (data) => {
  return (
    <div className="container-fluid">
      <div className="container">
        <div className="row">
          <nav className="navbar">
            <a className="navbar name" href="/">
              LensTest
            </a>
          </nav>
        </div>
      </div>
    </div>
  );
};

export default Nav;
