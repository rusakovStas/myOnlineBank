import React from "react";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import { library } from "@fortawesome/fontawesome-svg-core";
import { faArrowDown, faRubleSign } from "@fortawesome/free-solid-svg-icons";
import HomePage from "./components/Home/Page";
import LoginPage from "./components/Login/Page";
import UserPage from "./components/User/Page";
import GuestRoute from "./components/commons/GuestRoute";
import AdminRoute from "./components/commons/AdminRoute";
import UserRoute from "./components/commons/UserRoute";
import TopNavigationBar from "./components/commons/TopNavigationBar";
import AccountPage from "./components/Account/Page";
import ReduxToastr from "react-redux-toastr";

library.add(faArrowDown, faRubleSign);

const App = ({ location, isAuthentifacated }) => (
  <div>
    {isAuthentifacated && <TopNavigationBar />}
    <UserRoute location={location} path="/home" exact component={HomePage} />
    <UserRoute
      location={location}
      path="/accounts"
      exact
      component={AccountPage}
    />
    <AdminRoute location={location} path="/admin" exact component={UserPage} />
    <GuestRoute location={location} path="/" exact component={LoginPage} />
    {isAuthentifacated && (
      <ReduxToastr
        timeOut={4000}
        newestOnTop={false}
        position="top-right"
        transitionIn="fadeIn"
        transitionOut="fadeOut"
        closeOnToastrClick
      />
    )}
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
