import React from "react";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import HomePage from "./components/Home/Page";
import LoginPage from "./components/Login/Page";
import UserPage from "./components/User/Page";
import GuestRoute from "./components/commons/GuestRoute";
import AdminRoute from "./components/commons/AdminRoute";
import UserRoute from "./components/commons/UserRoute";
import TopNavigationBar from "./components/commons/TopNavigationBar";

const App = ({ location, isAuthentifacated }) => (
  <div>
    {isAuthentifacated && <TopNavigationBar />}
    <UserRoute location={location} path="/home" exact component={HomePage} />
    <AdminRoute location={location} path="/admin" exact component={UserPage} />
    <GuestRoute location={location} path="/" exact component={LoginPage} />
  </div>
);

App.propTypes = {
  location: PropTypes.shape({
    pathname: PropTypes.string.isRequired
  }).isRequired,
  isAuthentifacated: PropTypes.bool.isRequired
};

function mapStateToProps(store) {
  return {
    isAuthentifacated: !!store.user.access_token
  };
}

export default connect(mapStateToProps)(App);
