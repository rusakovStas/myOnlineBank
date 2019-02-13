import React from "react";
import {
    Button,
    Modal,
    ModalHeader,
    ModalBody,
    ModalFooter,
    Alert
} from "reactstrap";
import PropTypes from "prop-types";
import FormButton from "../../commons/FormButton";

class DeleteConfirm extends React.Component {
    state = {
        open: false,
        loading: false,
        errors: {}
    };

    close = () => this.setState({ open: false });

    show = val => this.setState({ open: val });

    onSubmit = () => {
        this.setState({ loading: true });
        this.props
            .submit(this.state.data)
            .catch(err => {
                this.setState({
                    errors: { global: err.response.data.message }
                });
                this.setState({ loading: false });
            })
            .finally(() => {
                if (!this.state.errors.global) this.setState({ open: false });
                this.setState({ loading: false });
            });
    };

    render() {
        const { errors, open } = this.state;
        const { color, enabled } = this.props;
        return (
            <div>
                <Button
                    block
                    color={color}
                    disabled={!enabled}
                    onClick={() => this.show(true)}
                >
                    Delete
                </Button>
                <Modal isOpen={open} toggle={() => this.show(!open)} centered>
                    <ModalHeader toggle={() => this.show(!open)}>
                        Confirm
                    </ModalHeader>
                    {errors.global && (
                        <Alert color="danger">{errors.global}</Alert>
                    )}
                    <ModalBody>Are you sure?</ModalBody>
                    <ModalFooter>
                        <FormButton
                            loading={this.state.loading}
                            variant="success"
                            submit={this.onSubmit}
                        >
                            Yes
                        </FormButton>
                    </ModalFooter>
                </Modal>
            </div>
        );
    }
}

DeleteConfirm.propTypes = {
    color: PropTypes.string.isRequired,
    enabled: PropTypes.bool.isRequired,
    submit: PropTypes.func.isRequired
};
export default DeleteConfirm;
