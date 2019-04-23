import React from "react";
import ScrollAnimation from "react-animate-on-scroll";
import AccountForMan from "../Account/AccountForMan";
import { Container, Row, Col, Button } from "reactstrap";

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
		stepFirst: false,
		stepSecond: false,
		stepThird: false
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

	stepThirdDone = () => {
		this.setState({ stepThird: true });
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
										This is your new mate.
										<p>He lives on tab "Accounts"</p>
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
										Just click on him and you'll see all
										what he can
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
										<h6>That was easy, isn't?</h6>
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
										Type new name of account and click on
										Accept button to confirm your change
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
										Click on Transaction button to send some
										money
									</h5>
								</Col>
								<Col md="4" className="mt-5">
									<p className="text-lg-left">
										1. Type user name (for example - Jhon)
										or 'My own account' if you want send
										money to your another account
									</p>
									<p className="text-lg-left">
										2. Type amount sum
									</p>
									<p className="text-lg-left">
										3. Click on accept button to send money
									</p>
								</Col>
								<Col md="8">
									<Container>
										<AccountForMan
											currentUser="you"
											account={this.state.account}
											toggle={() => {}}
											open
											enableTransaction
											enableEdit={false}
											enableBlock={false}
											hasRoleAdmin={false}
										/>
									</Container>
								</Col>
							</Row>
							{this.state.stepThird && (
								<div
									className="col-sm-12 mt-auto mb-3"
									style={{ zIndex: 200 }}
								>
									<p>
										<h6>That was easy, isn't?</h6>
										<h6>Let's move on</h6>
									</p>
								</div>
							)}
						</Container>
					</div>
				</section>
				<section className="account-section-fourth">
					<div className="row h-100">
						<div className="col-sm-12 my-auto">
							<h1>Block</h1>
						</div>
						<Container>
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
											You should be <b>Very Carefull</b>{" "}
											with this.
										</p>
										<p>
											When you delete account you lost all
											your money on him
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
				{false && (
					<section className="admin-section">
						<div className="row h-100">
							<div className="col-sm-12 my-auto">
								<ScrollAnimation animateIn="fadeIn">
									<h1>Instruction for Admin tab</h1>
								</ScrollAnimation>
							</div>
						</div>
					</section>
				)}
			</div>
		);
	}
}

export default HomePage;
