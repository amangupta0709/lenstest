import { Link } from "react-router-dom";
import "./Navbar.css";

const Nav = (data) => {
  return (
    <div className="container-fluid">
      <div className="container">
        <div className="row">
          <nav className="navbar">
            <Link className="navbar name" to="/">
              LensTest
            </Link>
          </nav>
        </div>
      </div>
    </div>
  );
};

export default Nav;
