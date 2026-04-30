# Rector Dashboard API Documentation

## Purpose
The Rector Dashboard API provides a high-level strategic overview of the university's key metrics. It is designed to assist the Rector (Admin) in making informed decisions by presenting aggregated data about the university's operations.

## Metrics Included
The `DashboardSummaryDTO` includes the following metrics:

1. **Total Number of Students**: Indicates the overall student population.
2. **Number of Active Students**: Shows the count of currently active students.
3. **Total Number of Teachers**: Reflects the total teaching staff.
4. **Total Number of Groups**: Represents the number of student groups.
5. **Total Number of Departments**: Provides the count of academic departments.
6. **Total Number of Specialties**: Displays the number of specialties offered.
7. **Additional Metrics**: Includes other strategic metrics such as the total number of users and subjects.

These metrics are crucial for understanding the university's scale, resource allocation, and operational efficiency.

## Access Control
The Rector Dashboard API is strictly secured and accessible only to users with the `SUPER_ADMIN` role. This ensures that sensitive data is protected and only available to authorized personnel.

## API Structure
- **Endpoint**: `GET /api/admin/dashboard/summary`
- **Authorization**: Requires `SUPER_ADMIN` role.
- **Response**: Returns a `DashboardSummaryDTO` containing the aggregated metrics.

### Example Response
```json
{
  "totalStudents": 5000,
  "activeStudents": 4500,
  "totalTeachers": 300,
  "totalGroups": 50,
  "totalDepartments": 10,
  "totalSpecialties": 20,
  "additionalMetrics": {
    "totalUsers": 5500,
    "totalSubjects": 100
  }
}
```

## Implementation Details
- **Controller**: `DashboardController`
  - Handles the `/api/admin/dashboard/summary` endpoint.
  - Secured with `@PreAuthorize("hasRole('SUPER_ADMIN')")`.
- **Service**: `DashboardService`
  - Aggregates data from repositories to populate the `DashboardSummaryDTO`.
- **DTO**: `DashboardSummaryDTO`
  - Encapsulates the metrics returned by the API.
