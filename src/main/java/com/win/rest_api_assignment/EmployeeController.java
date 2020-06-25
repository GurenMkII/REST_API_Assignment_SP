package com.win.rest_api_assignment;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.IanaLinkRelations;

@RestController
class EmployeeController {

  private final EmployeeRepository repository;

  private final EmployeeModelAssembler assembler;

  EmployeeController(EmployeeRepository repository, EmployeeModelAssembler assembler) {
    this.repository = repository;
    this.assembler = assembler;
  }

  // Aggregate root

//   @GetMapping("/employees")
//   List<Employee> all() {
//     return repository.findAll();
//   }

  @GetMapping("/employees")
    CollectionModel<EntityModel<Employee>> all() {

        List<EntityModel<Employee>> employees = repository.findAll().stream() //
        .map(assembler::toModel) //
        .collect(Collectors.toList());

        return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());

        // List<EntityModel<Employee>> employees = repository.findAll().stream()
        // .map(employee -> EntityModel.of(employee,
        //     linkTo(methodOn(EmployeeController.class).one(employee.getId())).withSelfRel(),
        //     linkTo(methodOn(EmployeeController.class).all()).withRel("employees")))
        // .collect(Collectors.toList());

        // return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
    }

    @PostMapping("/employees")
    ResponseEntity<?> newEmployee(@RequestBody Employee newEmployee) {

        EntityModel<Employee> entityModel = assembler.toModel(repository.save(newEmployee));

        return ResponseEntity //
            .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
            .body(entityModel);
}

//   @PostMapping("/employees")
//   Employee newEmployee(@RequestBody Employee newEmployee) {
//     return repository.save(newEmployee);
//   }

  // Single item

//   @GetMapping("/employees/{id}")
//   Employee one(@PathVariable Long id) {

//     return repository.findById(id)
//       .orElseThrow(() -> new EmployeeNotFoundException(id));
//   }

    @GetMapping("/employees/{id}")
    EntityModel<Employee> one(@PathVariable Long id) {

        Employee employee = repository.findById(id) //
            .orElseThrow(() -> new EmployeeNotFoundException(id));


        return assembler.toModel(employee);

        // return EntityModel.of(employee, //
        //     linkTo(methodOn(EmployeeController.class).one(id)).withSelfRel(),
        //     linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
}

  @PutMapping("/employees/{id}")
  ResponseEntity<?> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {

    Employee updatedEmployee = repository.findById(id) //
      .map(employee -> {
        employee.setName(newEmployee.getName());
        employee.setRole(newEmployee.getRole());
        return repository.save(employee);
      }) //
      .orElseGet(() -> {
        newEmployee.setId(id);
        return repository.save(newEmployee);
      });

    EntityModel<Employee> entityModel = assembler.toModel(updatedEmployee);

    return ResponseEntity //
      .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
      .body(entityModel);

    // return repository.findById(id)
    //   .map(employee -> {
    //     employee.setName(newEmployee.getName());
    //     employee.setRole(newEmployee.getRole());
    //     return repository.save(employee);
    //   })
    //   .orElseGet(() -> {
    //     newEmployee.setId(id);
    //     return repository.save(newEmployee);
    //   });
  }

  @DeleteMapping("/employees/{id}")
  ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
    repository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}