import { useState, useEffect } from "react";
import { Link } from "react-router";
import { ArrowLeft, ChevronLeft, ChevronRight, Plus } from "lucide-react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { BottomNav } from "../components/BottomNav";

const workoutDays = [5, 8, 12, 15, 19, 22, 26]; // Days with workouts
const currentDay = 21; // Today (March 21)

export function Calendar() {
  const [currentMonth] = useState("March 2026");
  const [scheduledWorkouts, setScheduledWorkouts] = useState<any[]>([]);

  useEffect(() => {
    // Load scheduled workouts from localStorage
    const saved = localStorage.getItem("scheduledWorkouts");
    if (saved) {
      const workouts = JSON.parse(saved);
      // Filter to only show user's own workouts (not client workouts)
      setScheduledWorkouts(workouts.filter((w: any) => w.clientId === "self"));
    }
  }, []);

  const renderCalendar = () => {
    const days = [];
    const daysInMonth = 31;
    const firstDay = 6; // March 1, 2026 is a Sunday (0-indexed, 6 = Saturday offset)

    // Empty cells for days before month starts
    for (let i = 0; i < firstDay; i++) {
      days.push(
        <div key={`empty-${i}`} className="aspect-square"></div>
      );
    }

    // Actual days
    for (let day = 1; day <= daysInMonth; day++) {
      const isWorkoutDay = workoutDays.includes(day);
      const isToday = day === currentDay;
      
      days.push(
        <div
          key={day}
          className={`
            aspect-square flex items-center justify-center rounded-lg
            ${isToday ? 'bg-primary text-primary-foreground' : ''}
            ${isWorkoutDay && !isToday ? 'bg-card border border-primary' : ''}
            ${!isWorkoutDay && !isToday ? 'text-muted-foreground' : ''}
          `}
        >
          <span className={isToday || isWorkoutDay ? 'text-white' : ''}>
            {day}
          </span>
        </div>
      );
    }

    return days;
  };

  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-4xl mx-auto space-y-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Link to="/dashboard">
                <button className="text-white hover:text-primary">
                  <ArrowLeft className="w-6 h-6" />
                </button>
              </Link>
              <h1 className="text-white">Workout Calendar</h1>
            </div>
            <Link to="/schedule-workout">
              <Button
                size="sm"
                className="bg-primary hover:bg-primary/90 text-primary-foreground"
              >
                <Plus className="w-4 h-4 mr-2" />
                Schedule
              </Button>
            </Link>
          </div>

          <Card className="bg-card border-border p-6">
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <Button 
                  variant="ghost" 
                  size="icon"
                  className="text-white hover:bg-card/50"
                >
                  <ChevronLeft className="w-5 h-5" />
                </Button>
                <h2 className="text-white">{currentMonth}</h2>
                <Button 
                  variant="ghost" 
                  size="icon"
                  className="text-white hover:bg-card/50"
                >
                  <ChevronRight className="w-5 h-5" />
                </Button>
              </div>

              <div className="grid grid-cols-7 gap-2">
                {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
                  <div key={day} className="text-center text-muted-foreground pb-2">
                    {day}
                  </div>
                ))}
                {renderCalendar()}
              </div>
            </div>
          </Card>

          <div className="space-y-3">
            <h3 className="text-white">Legend</h3>
            <div className="flex flex-wrap gap-4">
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded bg-primary"></div>
                <span className="text-muted-foreground">Today</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded bg-card border border-primary"></div>
                <span className="text-muted-foreground">Workout completed</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded bg-transparent border border-muted-foreground/30"></div>
                <span className="text-muted-foreground">No activity</span>
              </div>
            </div>
          </div>

          <Card className="bg-card border-border p-6">
            <div className="space-y-4">
              <h3 className="text-white">Upcoming Workouts</h3>
              <div className="space-y-3">
                {scheduledWorkouts.map((workout: any) => (
                  <div key={workout.id} className="flex items-center justify-between p-3 bg-background rounded-lg">
                    <div>
                      <p className="text-white">{workout.name}</p>
                      <p className="text-muted-foreground">{workout.date}, {workout.time}</p>
                    </div>
                    <span className="text-primary">Scheduled</span>
                  </div>
                ))}
                <div className="flex items-center justify-between p-3 bg-background rounded-lg">
                  <div>
                    <p className="text-white">Upper Body Circuit</p>
                    <p className="text-muted-foreground">Tomorrow, 6:00 AM</p>
                  </div>
                  <span className="text-primary">Scheduled</span>
                </div>
                <div className="flex items-center justify-between p-3 bg-background rounded-lg">
                  <div>
                    <p className="text-white">Cardio Session</p>
                    <p className="text-muted-foreground">Mar 20, 5:30 PM</p>
                  </div>
                  <span className="text-primary">Scheduled</span>
                </div>
              </div>
            </div>
          </Card>
        </div>
      </div>
      <BottomNav />
    </>
  );
}