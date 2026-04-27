import { useState } from "react";
import { Link } from "react-router";
import { User, MessageCircle, Calendar, Plus } from "lucide-react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { BottomNav } from "../components/BottomNav";

const mockClients = [
  {
    id: 1,
    name: "John Smith",
    email: "john@example.com",
    workoutsThisWeek: 3,
    nextSession: "March 22, 2026",
  },
  {
    id: 2,
    name: "Sarah Johnson",
    email: "sarah@example.com",
    workoutsThisWeek: 4,
    nextSession: "March 23, 2026",
  },
  {
    id: 3,
    name: "Mike Williams",
    email: "mike@example.com",
    workoutsThisWeek: 2,
    nextSession: "March 24, 2026",
  },
];

export function Clients() {
  const [clients] = useState(mockClients);

  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-4xl mx-auto space-y-6">
          <div className="flex items-center justify-between">
            <h1 className="text-white">Clients</h1>
            <Button
              size="sm"
              className="bg-primary hover:bg-primary/90 text-primary-foreground"
            >
              <Plus className="w-4 h-4 mr-2" />
              Add Client
            </Button>
          </div>

          <div className="space-y-4">
            {clients.map((client) => (
              <Card key={client.id} className="bg-card border-border p-6">
                <div className="space-y-4">
                  <div className="flex items-start justify-between">
                    <div className="flex gap-4">
                      <div className="w-12 h-12 rounded-full bg-primary/20 flex items-center justify-center flex-shrink-0">
                        <User className="w-6 h-6 text-primary" />
                      </div>
                      <div>
                        <h3 className="text-white mb-1">{client.name}</h3>
                        <p className="text-muted-foreground text-sm">{client.email}</p>
                      </div>
                    </div>
                    <Link to={`/messages?client=${client.id}`}>
                      <Button
                        size="sm"
                        variant="outline"
                        className="border-border text-white hover:bg-card"
                      >
                        <MessageCircle className="w-4 h-4" />
                      </Button>
                    </Link>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="bg-background rounded-lg p-3 border border-border">
                      <p className="text-muted-foreground text-sm">Workouts This Week</p>
                      <p className="text-white text-xl">{client.workoutsThisWeek}</p>
                    </div>
                    <div className="bg-background rounded-lg p-3 border border-border">
                      <p className="text-muted-foreground text-sm">Next Session</p>
                      <p className="text-white text-sm">{client.nextSession}</p>
                    </div>
                  </div>

                  <div className="flex gap-3">
                    <Link to={`/schedule-workout?client=${client.id}`} className="flex-1">
                      <Button
                        className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
                      >
                        <Calendar className="w-4 h-4 mr-2" />
                        Schedule Workout
                      </Button>
                    </Link>
                    <Link to={`/client-profile?id=${client.id}`}>
                      <Button
                        variant="outline"
                        className="border-border text-white hover:bg-card"
                      >
                        View Profile
                      </Button>
                    </Link>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </div>
      </div>
      <BottomNav />
    </>
  );
}
