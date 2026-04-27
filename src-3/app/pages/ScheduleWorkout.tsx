import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { ArrowLeft, Calendar as CalendarIcon } from "lucide-react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { BottomNav } from "../components/BottomNav";

const activityOptions = [
  "Running",
  "Cycling",
  "Weightlifting",
  "Hiking",
  "Swimming",
  "Yoga",
];

export function ScheduleWorkout() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const clientId = searchParams.get("client");
  const isBusinessAccount = !!clientId;

  const [formData, setFormData] = useState({
    date: "",
    time: "",
    activity: "",
    duration: "",
    notes: "",
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Save scheduled workout
    const scheduledWorkout = {
      id: Date.now(),
      ...formData,
      clientId: clientId || "self",
      createdAt: new Date().toISOString(),
    };

    const existing = localStorage.getItem("scheduledWorkouts");
    const workouts = existing ? JSON.parse(existing) : [];
    workouts.push(scheduledWorkout);
    localStorage.setItem("scheduledWorkouts", JSON.stringify(workouts));

    // Navigate back
    if (isBusinessAccount) {
      navigate("/clients");
    } else {
      navigate("/calendar");
    }
  };

  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-2xl mx-auto space-y-6">
          <div className="flex items-center gap-4">
            <Link to={isBusinessAccount ? "/clients" : "/calendar"}>
              <button className="text-white hover:text-primary">
                <ArrowLeft className="w-6 h-6" />
              </button>
            </Link>
            <div>
              <h1 className="text-white">Schedule Workout</h1>
              {isBusinessAccount && (
                <p className="text-muted-foreground text-sm">
                  For Client #{clientId}
                </p>
              )}
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <Card className="bg-card border-border p-6 space-y-4">
              <div className="space-y-2">
                <Label htmlFor="date" className="text-white">
                  Date
                </Label>
                <Input
                  id="date"
                  type="date"
                  value={formData.date}
                  onChange={(e) =>
                    setFormData({ ...formData, date: e.target.value })
                  }
                  className="bg-input-background border-border text-white"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="time" className="text-white">
                  Time
                </Label>
                <Input
                  id="time"
                  type="time"
                  value={formData.time}
                  onChange={(e) =>
                    setFormData({ ...formData, time: e.target.value })
                  }
                  className="bg-input-background border-border text-white"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="activity" className="text-white">
                  Activity
                </Label>
                <select
                  id="activity"
                  value={formData.activity}
                  onChange={(e) =>
                    setFormData({ ...formData, activity: e.target.value })
                  }
                  className="w-full bg-input-background border border-border rounded-lg px-4 py-2 text-white"
                  required
                >
                  <option value="">Select activity</option>
                  {activityOptions.map((activity) => (
                    <option key={activity} value={activity}>
                      {activity}
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="duration" className="text-white">
                  Duration (minutes)
                </Label>
                <Input
                  id="duration"
                  type="number"
                  value={formData.duration}
                  onChange={(e) =>
                    setFormData({ ...formData, duration: e.target.value })
                  }
                  className="bg-input-background border-border text-white"
                  placeholder="60"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="notes" className="text-white">
                  Notes (Optional)
                </Label>
                <textarea
                  id="notes"
                  value={formData.notes}
                  onChange={(e) =>
                    setFormData({ ...formData, notes: e.target.value })
                  }
                  className="w-full bg-input-background border border-border rounded-lg px-4 py-2 text-white min-h-[100px]"
                  placeholder="Add any notes or instructions..."
                />
              </div>
            </Card>

            <Button
              type="submit"
              className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
            >
              <CalendarIcon className="w-4 h-4 mr-2" />
              Schedule Workout
            </Button>
          </form>
        </div>
      </div>
      <BottomNav />
    </>
  );
}
