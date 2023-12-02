package bogen.studio.Room.exceptionHandler;

import bogen.studio.Room.Exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static my.common.commonkoochita.Utility.Utility.generateErr;

@RestControllerAdvice
public class ExceptionHandlerClass {

    @ExceptionHandler(value = InvalidIdException.class)
    public ResponseEntity<String> idInvalidExceptionHandler(InvalidIdException e) {

        return ResponseEntity.ok(generateErr(e.getMessage()));
    }

    @ExceptionHandler(value = NoAdultsInPassengersException.class)
    public ResponseEntity<String> noAdultsInPassengersExceptionHandler(NoAdultsInPassengersException e) {

        return ResponseEntity.ok(generateErr(e.getMessage()));
    }

    @ExceptionHandler(value = RoomUnavailableByOwnerException.class)
    public ResponseEntity<String> roomUnavailableByOwnerExceptionHandler(RoomUnavailableByOwnerException e) {

        return ResponseEntity.ok(generateErr(e.getMessage()));
    }

    @ExceptionHandler(value = RoomNotFreeException.class)
    public ResponseEntity<String> roomNotFreeExceptionHandler(RoomNotFreeException e) {

        return ResponseEntity.ok(generateErr(e.getMessage()));
    }

    @ExceptionHandler(value = BackendErrorException.class)
    public ResponseEntity<String> backendErrorExceptionHandler(BackendErrorException e) {

        return ResponseEntity.ok(generateErr(e.getMessage()));
    }

    @ExceptionHandler(value = InvalidInputException.class)
    public ResponseEntity<String> invalidInputExceptionHandler(InvalidInputException e) {

        return ResponseEntity.ok(generateErr(e.getMessage()));
    }

    @ExceptionHandler(value = DocumentVersionChangedException.class)
    public ResponseEntity<String> documentVersionChangedExceptionHandler(DocumentVersionChangedException e) {

        return ResponseEntity.ok(generateErr(e.getMessage()));
    }

    @ExceptionHandler(value = InvalidRequestByCustomerException.class)
    public ResponseEntity<String> invalidRequestByCustomerExceptionHandler(InvalidRequestByCustomerException e) {

        return ResponseEntity.ok(generateErr(e.getMessage()));
    }

}
