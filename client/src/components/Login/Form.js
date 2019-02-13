import React from "react";
import {
	Alert,
	Container,
	Row,
	Col,
	Input,
	Card,
	CardBody,
	Label,
	Form,
	FormGroup,
	InputGroup
} from "reactstrap";
import PropTypes from "prop-types";
import InlineError from "../commons/InlineError";
import FormButton from "../commons/FormButton";

class LoginForm extends React.Component {
	state = {
		data: {
			email: "",
			password: ""
		},
		loading: false,
		errors: {}
	};

	onChange = e =>
		this.setState({
			data: { ...this.state.data, [e.target.name]: e.target.value }
		});

	onSubmit = () => {
		const errors = this.validate(this.state.data);
		this.setState({ errors });
		if (Object.keys(errors).length === 0) {
			this.setState({ loading: true });
			this.props.submit(this.state.data).catch(err => {
				this.setState({
					errors: { global: err.response.data.error },
					loading: false
				});
			});
		}
	};

	validate = data => {
		const errors = {};
		if (!data.email) errors.email = "Can't be blank";
		if (!data.password) errors.password = "Can't be blank";
		return errors;
	};

	render() {
		const { data, errors, loading } = this.state;
		return (
			<Container>
				<Row className="d-flex justify-content-center mt-5">
					<Col md="5">
						<Card className="mt-5 shadow">
							<CardBody>
								<Form>
									<p className="h4 text-center py-4">
										Sign in
									</p>
									<div className="grey-text">
										{errors.global && (
											<Alert color="danger">
												{errors.global}
											</Alert>
										)}
										<FormGroup>
											<Label for="email">Username</Label>
											<InputGroup>
												<Input
													placeholder="Type your username"
													type="text"
													validate
													error="wrong"
													success="right"
													id="email"
													name="email"
													value={data.email}
													onChange={this.onChange}
													disabled={loading}
												/>
											</InputGroup>
											{errors.email && (
												<InlineError
													text={errors.email}
												/>
											)}
										</FormGroup>
										<FormGroup>
											<Label for="password">
												Password
											</Label>
											<InputGroup>
												<Input
													placeholder="Type your password"
													id="password"
													name="password"
													type="password"
													value={data.password}
													onChange={this.onChange}
													disabled={loading}
												/>
											</InputGroup>
										</FormGroup>
										{errors.password && (
											<InlineError
												text={errors.password}
											/>
										)}
									</div>
									<div className="text-center">
										<FormButton
											loading={loading}
											variant="primary"
											size="lg"
											submit={this.onSubmit}
										>
											Login
										</FormButton>
									</div>
								</Form>
							</CardBody>
						</Card>
					</Col>
				</Row>
			</Container>
		);
	}
}

LoginForm.propTypes = {
	submit: PropTypes.func.isRequired
};

export default LoginForm;
