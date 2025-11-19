import React from "react";
import { Link } from "react-router-dom";
import styles from "./NavbarStyle.module.css";
import logo from "./TournamentHostLogo.jpg";

function Navbar({ isOpen, onClose }) {
    return (
        <>
            {isOpen && <div className={styles.overlay} onClick={onClose} />}
            <nav className={`${styles.Navbar} ${isOpen ? styles.open : ''}`}>
                <div className={styles.Logo}>
                    <img src={logo} alt="Tournament Host Logo" />
                </div>
                <ul className={styles.NavList}>
                    <li className={styles.NavItem}>
                        <Link to="/" className={styles.NavButton} onClick={onClose}>Home</Link>
                    </li>
                    <li className={styles.NavItem}>
                        <Link to="/tournaments" className={styles.NavButton} onClick={onClose}>Tournaments</Link>
                    </li>
                    <li className={styles.NavItem}>
                        <Link to="/leagues" className={styles.NavButton} onClick={onClose}>Leagues</Link>
                    </li>
                </ul>
            </nav>
        </>
    );
}

export default Navbar;