package bogen.studio.Room.exceptionHandler;

import bogen.studio.Room.Exception.IdInvalidException;
import bogen.studio.Room.Exception.NoAdultsInPassengersException;
import bogen.studio.Room.Exception.RoomNotFreeException;
import bogen.studio.Room.Exception.RoomUnavailableByOwnerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static my.common.commonkoochita.Utility.Utility.generateErr;

@RestControllerAdvice
public class ExceptionHandlerClass {

    @ExceptionHandler(value = IdInvalidException.class)
    public ResponseEntity<String> idInvalidExceptionHandler(IdInvalidException e) {

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

}
