import { Link } from "react-router-dom";
import "./Navbar.css";

const Nav = () => {
  return (
    <div className="container-fluid">
      <div className="container">
        <div className="row">
          <nav className="navbar">
            <Link className="navbar name" to="/">
              <img src={`${process.env.PUBLIC_URL}/logo.png`} alt="logo" height={70}/> LensTest
            </Link>
          </nav>
        </div>
      </div>
    </div>
  );
};

export default Nav;
