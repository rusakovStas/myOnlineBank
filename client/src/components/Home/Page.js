import React from "react";
import { toastr } from "react-redux-toastr";
import { Container, Row, Col } from "reactstrap";
import { PropTypes } from "prop-types";
import { connect } from "react-redux";
import AccountForMan from "../Account/AccountForMan";

class HomePage extends React.Component {
	state = {
		open: false,
		account: {
			name: "Your account",
			amount: { sum: "1000" },
			user: { username: "you" },
			number: "5469 0600 1234 5678",
			id: "1"
		},
		suggestions: [
			{ maskAccountNumber: "**** 1223", userName: "Jhon" },
			{ maskAccountNumber: "**** 9999", userName: "My own account" }
		],
		stepFirst: false,
		stepSecond: false,
		stepThird: false,
		stepFourth: false
	};

	toggle = () => {
		this.setState({ open: true, stepFirst: true });
	};

	edit = newName => {
		this.setState({
			account: { ...this.state.account, name: newName },
			stepSecond: true
		});
	};

	transaction = sum => {
		const newSum = Number(this.state.account.amount.sum) - Number(sum);
		this.setState(
			{
				account: { ...this.state.account, amount: { sum: newSum } },
				stepThird: true
			},
			() => toastr.success("Message from server", "You did it!")
		);
	};

	block = () => {
		this.setState({ stepFourth: true });
	};

	stepThirdDone = () => {
		this.setState({ stepThird: true });
	};

	getTitleForStepFour = () => {
		return this.props.hasRoleAdmin ? "All done... almost" : "All done";
	};

	render() {
		return (
			<div>
				<section className="intro-section">
					<div className="row h-100">
						<div className="col-sm-12 my-auto">
							<h1>Welcome to innovation online bank!</h1>
						</div>
						<div className="col-sm-12 mt-auto">
							<h6>Scrool below to see instruction</h6>
						</div>
					</div>
				</section>

				<section className="account-section">
					<div className="row h-100">
						<div className="col-sm-12 my-auto">
							<h1>Instruction for Accounts tab</h1>
						</div>
						<Container>
							<Row>
								<Col
									sm="12"
									md={{ size: 10, offset: 1 }}
									className="mb-2 mt-2"
								>
									<h6>
										There is your new mate.
										<p>He lives on the Accounts tab</p>
									</h6>
								</Col>

								<Col
									sm="12"
									md={{ size: 10, offset: 1 }}
									className="mb-3"
								>
									<Container>
										<AccountForMan
											currentUser="you"
											account={this.state.account}
											toggle={() => {}}
											open={false}
											hasRoleAdmin={false}
										/>
									</Container>
								</Col>
							</Row>
						</Container>
						<div
							className="col-sm-12 mt-auto mb-3"
							style={{ zIndex: 200 }}
						>
							<h6>Let's see what we can do</h6>
						</div>
					</div>
				</section>
				<section className="account-section-first">
					<div className="row h-100">
						<div className="col-sm-12 my-auto">
							<h1>Just click</h1>
						</div>
						<Container>
							<Row>
								<Col
									sm="12"
									md={{ size: 10, offset: 1 }}
									className="mb-2 mt-2"
								>
									<h5 className="mb-4">
										Just click on it and you will see all
										what it can
									</h5>
								</Col>

								<Col
									sm="12"
									md={{ size: 10, offset: 1 }}
									className="mb-3"
								>
									<Container>
										<AccountForMan
											currentUser="you"
											account={this.state.account}
											toggle={this.toggle}
											open={this.state.open}
											enableTransaction={false}
											enableEdit={false}
											enableBlock={false}
											hasRoleAdmin={false}
										/>
									</Container>
								</Col>
							</Row>
							{this.state.stepFirst && (
								<div
									className="col-sm-12 mt-auto mb-3"
									style={{ zIndex: 200 }}
								>
									<p>
										<h6>That was easy, right?</h6>
										<h6>Let's move on</h6>
									</p>
								</div>
							)}
						</Container>
					</div>
				</section>
				<section className="account-section-second">
					<div className="row h-100">
						<div className="col-sm-12 my-auto">
							<h1>Edit</h1>
						</div>
						<Container>
							<Row>
								<Col
									sm="12"
									md={{ size: 10, offset: 1 }}
									className="mb-2 mt-2"
								>
									<h5 className="mb-4">
										Click on edit button to... edit your
										account
									</h5>
									<p>
										Enter a new account name and click on
										Accept button to confirm your changes
									</p>
								</Col>

								<Col
									sm="12"
									md={{ size: 10, offset: 1 }}
									className="mb-3"
								>
									<Container>
										<AccountForMan
											currentUser="you"
											account={this.state.account}
											toggle={() => {}}
											open
											enableTransaction={false}
											enableEdit
											edit={this.edit}
											enableBlock={false}
											hasRoleAdmin={false}
										/>
									</Container>
								</Col>
							</Row>
							{this.state.stepSecond && (
								<div
									className="col-sm-12 mt-auto mb-3"
									style={{ zIndex: 200 }}
								>
									<p>
										<h6>Greate new name!</h6>
										<h6>Let's move on</h6>
									</p>
								</div>
							)}
						</Container>
					</div>
				</section>
				<section className="account-section-third">
					<div className="row h-100">
						<div className="col-sm-12 my-auto">
							<h1>Transaction</h1>
						</div>
						<Container>
							<Row className="p-0">
								<Col
									sm="12"
									md={{ size: 10, offset: 1 }}
									className="mb-2 mt-2"
								>
									<h5 className="mb-4">
										Click on the Transaction button to send
										some money
									</h5>
								</Col>
								<Col md="4" className="mt-5">
									<Container>
										<p className="text-lg-left">
											1. Type user name, for example -
											Jhon, or My own account if you want
											send money to your own account
										</p>
										<p className="text-lg-left">
											2. Enter amount sum
										</p>
										<p className="text-lg-left">
											3. Click on the Accept button to
											send money
										</p>
									</Container>
								</Col>
								<Col md="8">
									<Container>
										<AccountForMan
											currentUser="you"
											account={this.state.account}
											toggle={() => {}}
											suggestions={this.state.suggestions}
											open
											enableTransaction
											transaction={this.transaction}
											enableEdit={false}
											enableBlock={false}
											hasRoleAdmin={false}
										/>
									</Container>
								</Col>
							</Row>
							{this.state.stepThird && (
								<Row>
									<Col md="12" className="mt-5">
										<p>
											<h6>
												Don't worry it was not real
												money ;)
											</h6>
											<h6>We are about finish</h6>
										</p>
									</Col>
								</Row>
							)}
						</Container>
					</div>
				</section>
				<section className="account-section-fourth">
					<div className="row h-100">
						<div className="col-sm-12 my-auto">
							<h1 className="mt-5">
								{this.state.stepFourth
									? this.getTitleForStepFour()
									: "Block"}
							</h1>
						</div>
						<Container>
							{this.state.stepFourth ? (
								<div
									className="col-sm-12 mt-auto mb-3"
									style={{ zIndex: 200 }}
								>
									<h6>
										Yes, it's just disappear
										<p>But don't worry</p>
										<p>It was just a training ;)</p>
										<p>But we have a real gift for you</p>
										<p>
											On tab Account a new brilliant
											account with some money is waiting
											for you!
										</p>
										<p>We hope you enjoy working with us</p>
										{this.props.hasRoleAdmin && (
											<p>
												P.S. take a look at the last
												part of instruction below
											</p>
										)}
									</h6>
								</div>
							) : (
								<Row>
									<Col
										sm="12"
										md={{ size: 10, offset: 1 }}
										className="mb-2 mt-2"
									>
										<h6>
											Click on block button to delete your
											account
											<p>
												You should be{" "}
												<b>Very Carefull</b> with this.
											</p>
											<p>
												When you delete an account you{" "}
												<b>
													lose all the money that was
													on it
												</b>
											</p>
										</h6>
									</Col>

									<Col
										sm="12"
										md={{ size: 10, offset: 1 }}
										className="mb-3"
									>
										<Container>
											<AccountForMan
												currentUser="you"
												account={this.state.account}
												toggle={this.toggle}
												open
												enableTransaction={false}
												enableEdit={false}
												enableBlock
												block={this.block}
												hasRoleAdmin={false}
											/>
										</Container>
									</Col>
								</Row>
							)}
						</Container>
					</div>
				</section>
				{this.props.hasRoleAdmin && (
					<section className="admin-section">
						<div className="row h-100">
							<Container>
								<div className="col-sm-12 my-auto">
									<h1 className="mt-5">
										Who is the Boss here?
									</h1>
									<h2>It's you!</h2>
									<p>Yes, you have some special abilities</p>
									<Row>
										<Col
											sm="12"
											md={{ size: 10, offset: 1 }}
											className="mb-2 mt-2"
										>
											<h6>
												For example, all your accounts
												with no limit amount of money
											</h6>
										</Col>

										<Col
											sm="12"
											md={{ size: 10, offset: 1 }}
											className="mb-3"
										>
											<Container>
												<AccountForMan
													currentUser="you"
													account={this.state.account}
													toggle={this.toggle}
													open={false}
													enableTransaction={false}
													enableEdit={false}
													enableBlock={false}
													block={() => {}}
													hasRoleAdmin
												/>
											</Container>
										</Col>
									</Row>
									<Row>
										<Col md="12" className="mt-5">
											<p>
												<h6>
													And also you can do
													transaction under any
													another user
												</h6>
												<h6>
													Even if amount of
													transaction is more than he
													have
												</h6>
												<h6>
													Use it carefully and don't
													forget:
												</h6>
												<h4>
													With great power comes great
													responsibility...
												</h4>
												<h4>but it's still cool ;)</h4>
											</p>
										</Col>
									</Row>
								</div>
							</Container>
						</div>
					</section>
				)}
			</div>
		);
	}
}

HomePage.propTypes = {
	hasRoleAdmin: PropTypes.bool.isRequired
};

function mapStateToProps(store) {
	return {
		hasRoleAdmin:
			!!store.user.roles &&
			!!store.user.roles.find(element => element === "ROLE_ADMIN")
	};
}

export default connect(mapStateToProps)(HomePage);
