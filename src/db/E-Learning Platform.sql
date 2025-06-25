CREATE TYPE UserRole AS ENUM (
  'ADMIN',
  'TRAINER',
  'STUDENT'
);

CREATE TYPE UserStatus AS ENUM (
  'ACTIVE',
  'IN_ACTIVE',
  'SUSPENDED'
);

CREATE TYPE ResourseStatus AS ENUM (
  'DRAFT',
  'PUBLISHED',
  'ARCHIVE'
);

CREATE TYPE CompletionStatus AS ENUM (
  'NOT_STARTED',
  'IN_PROGRESS',
  'COMPLETED'
);

CREATE TYPE CourseLevel AS ENUM (
  'BEGINNER',
  'INTERMEDIATE',
  'ADVANCED'
);

CREATE TYPE CourseType AS ENUM (
  'LIVE',
  'SELF_PACED'
);

CREATE TYPE ScheduleType AS ENUM (
  'WEEKDAYS_ONLY',
  'WEEKENDS_ONLY',
  'DAILY'
);

CREATE TYPE BatchStatus AS ENUM (
  'DRAFT',
  'ACTIVE',
  'COMPLETED',
  'CANCELLED'
);

CREATE TYPE PaymentStatus AS ENUM (
  'SUCCESS',
  'PENDING',
  'FAILED'
);

CREATE TYPE EnrollmentStatus AS ENUM (
  'PAYMENT_FAILED',
  'ASSIGNED',
  'NOT_ASIGNED'
);

-- ------------------------ USERS

CREATE TABLE "User" (
  id uuid PRIMARY KEY,
  firstName varchar NOT NULL,
  lastName varchar NOT NULL,
  email varchar UNIQUE NOT NULL, -- index
  role UserRole,
  hashedPassword varchar NOT NULL,
  lastLoginAt timestamp NOT NULL,
  status UserStatus
);

CREATE TABLE Education (
  id int PRIMARY KEY,
  trainerId uuid REFERENCES "User"(id) ON DELETE CASCADE,
  institution varchar NOT NULL,
  degree varchar NOT NULL,
  startMonth int NOT NULL,
  startYear int NOT NULL,
  endMonth int,
  endYear int,
  isCurrent boolean
);

CREATE TABLE WorkExperience (
  id int PRIMARY KEY,
  trainerId uuid REFERENCES "User"(id) ON DELETE CASCADE,
  company varchar NOT NULL,
  designation varchar NOT NULL,
  startMonth int NOT NULL,
  startYear int NOT NULL,
  endMonth int,
  endYear int,
  isCurrent boolean
);

CREATE TABLE Trainer (
  trainerId uuid UNIQUE REFERENCES "User"(id) ON DELETE CASCADE,
  technicalSkills varchar[] NOT NULL,
  softSkills varchar[]
);

CREATE TABLE Student (
  studentId uuid UNIQUE REFERENCES "User"(id) ON DELETE CASCADE,
  gitHubUrl varchar,
  linkedInUrl varchar
);

-- ------------------------ COURSE

CREATE TABLE Lesson (
  id SERIAL PRIMARY KEY,
  sequenceNumber int NOT NULL,
  moduleId int REFERENCES Module(id) ON DELETE CASCADE,
  title varchar NOT NULL,
  resourse varchar NOT NULL,
  duration int NOT NULL,
  status ResourseStatus
);

CREATE TABLE Module (
  id SERIAL PRIMARY KEY,
  sequenceNumber int NOT NULL,
  courseId int REFERENCES Course(id) ON DELETE CASCADE,
  title varchar NOT NULL,
  description text,
  duration int NOT NULL,
  status ResourseStatus
);

CREATE TABLE Course (
  id SERIAL PRIMARY KEY,
  createdBy uuid REFERENCES "User"(id) ON DELETE SET NULL,
  title varchar NOT NULL,
  description text NOT NULL,
  skills varchar[] NOT NULL,
  category varchar NOT NULL,
  courseLevel CourseLevel,
  courseType CourseType,
  prerequisites varchar[],
  staus ResourseStatus,
  isFreeCourse boolean,
  duration int NOT NULL
);

CREATE TABLE Category (
  id SERIAL PRIMARY KEY,
  name varchar -- index
);

-- CREATE TABLE CourseCategoryMapping (
--   id int PRIMARY KEY,
--   courseId int NOT NULL,
--   categoryId int NOT NULL
-- );

CREATE TABLE PriceDetails (
  id SERIAL PRIMARY KEY,
  courseId int REFERENCES Course(id) ON DELETE CASCADE,
  currencyCode text,
  currencySymbol text,
  amount double precision
);

CREATE TABLE CourseEnrollment (
  id SERIAL PRIMARY KEY,
  courseId int REFERENCES Course(id) ON DELETE CASCADE,
  studentId uuid REFERENCES "User"(id) ON DELETE SET NULL,
  staus EnrollmentStatus
);

CREATE TABLE PaymentDetails (
  id SERIAL PRIMARY KEY,
  enrollmentId int REFERENCES CourseEnrollment(id) ON DELETE CASCADE,
  currencyCode int NOT NULL,
  amount double precision
);

CREATE TABLE StudentLessonProgress (
  id SERIAL PRIMARY KEY,
  courseId int REFERENCES Course(id) ON DELETE SET NULL,
  lessonId int REFERENCES Lesson(id) ON DELETE SET NULL,
  studentId uuid REFERENCES "User"(id) ON DELETE SET NULL,
  status CompletionStatus,
  completedDateTime timestamp
);

-- ------------------------ BATCH COURSE

CREATE TABLE BatchCourseDetails (
  batchcourseId int UNIQUE NOT NULL,
  totalDays int,
  startDate date,
  endDate date,
  scheduleType ScheduleType,
  status BatchStatus
);

CREATE TABLE BatchSchedule (
  id int PRIMARY KEY,
  batchCourseId int NOT NULL,
  moduleId int NOT NULL,
  lessonId int NOT NULL,
  tutorId uuid NOT NULL,
  scheduledDate date,
  startTime time,
  endTime time,
  staus CompletionStatus
);

CREATE TABLE BatchToUsersMapping (
  id int PRIMARY KEY,
  batchCourseId int NOT NULL,
  userId uuid NOT NULL,
  role UserRole
);


ALTER TABLE BatchCourseDetails ADD CONSTRAINT BatchCourse FOREIGN KEY (batchcourseId) REFERENCES Course (id);

ALTER TABLE BatchSchedule ADD CONSTRAINT BatchSchedule FOREIGN KEY (batchCourseId) REFERENCES Course (id);

ALTER TABLE BatchSchedule ADD CONSTRAINT BatchSchedule FOREIGN KEY (moduleId) REFERENCES Module (id);

ALTER TABLE BatchSchedule ADD CONSTRAINT BatchSchedule FOREIGN KEY (lessonId) REFERENCES Lesson (id);

ALTER TABLE BatchSchedule ADD CONSTRAINT BatchSchedule FOREIGN KEY (tutorId) REFERENCES User (id);

ALTER TABLE BatchToUsersMapping ADD CONSTRAINT BatchEnrollment FOREIGN KEY (batchCourseId) REFERENCES Course (id);

ALTER TABLE BatchToUsersMapping ADD CONSTRAINT BatchEnrollment FOREIGN KEY (userId) REFERENCES User (id);
