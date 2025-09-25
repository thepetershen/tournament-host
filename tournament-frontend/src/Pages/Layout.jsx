import React from "react";
import Navbar from "../Components/NavBar/Navbar";
import TopBar from "../Components/TopBar/TopBar";
import { Outlet } from "react-router-dom";

function Layout() {
    return (
        <div style={{ display: "flex" }}>
            <Navbar />
            <div style={{ marginLeft: "400px", width: "100%" }}>
                <TopBar />
                <div style={{ marginTop: "60px" }}>
                  <Outlet />
                </div>
            </div>
        </div>
    );
}

export default Layout;