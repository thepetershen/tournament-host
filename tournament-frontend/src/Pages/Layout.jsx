import React from "react";
import Navbar from "../Components/NavBar/Navbar";
import { Outlet } from "react-router-dom";

function Layout() {
    return (
        <div style={{ display: "flex" }}>
            <Navbar />
            <div style={{ marginLeft: "400px", width: "100%" }}>
                <Outlet />
            </div>
        </div>
    );
}

export default Layout;