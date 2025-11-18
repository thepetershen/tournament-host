import React, { useState, useEffect } from "react";
import Navbar from "../Components/NavBar/Navbar";
import TopBar from "../Components/TopBar/TopBar";
import { Outlet } from "react-router-dom";
import styles from "./Layout.module.css";

function Layout() {
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    const closeSidebar = () => {
        setSidebarOpen(false);
    };

    return (
        <div className={styles.layoutRoot}>
            <Navbar isOpen={sidebarOpen} onClose={closeSidebar} />
            <div className={styles.contentArea}>
                <TopBar onMenuToggle={toggleSidebar} />
                <Outlet />
            </div>
        </div>
    );
}

export default Layout;