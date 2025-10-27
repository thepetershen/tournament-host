import React from "react";
import { Link } from "react-router-dom";
import styles from "./NavbarStyle.module.css";
import logo from "./PLACEHOLDER LOGO.png";

function Navbar() {
    return (
        <nav className={styles.Navbar}>
            <div className={styles.Logo}>
                <img src={logo} alt="Tournament Host Logo" />
            </div>
            <ul className={styles.NavList}>
                <li className={styles.NavItem}>
                    <Link to="/" className={styles.NavButton}>Home</Link>
                </li>
                <li className={styles.NavItem}>
                    <Link to="/tournament" className={styles.NavButton}>Tournaments</Link>
                </li>
            </ul>
        </nav>
    );
}

export default Navbar;