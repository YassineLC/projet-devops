import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { api, dateFr, type Emprunt, type Statut } from '@/api'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'

const LIBELLES: Record<Statut, string> = { EN_COURS: 'En cours', EN_RETARD: 'En retard', RENDU: 'Rendu' }

type Props = { nom: string; onNom: (nom: string) => void }

export function MesEmprunts({ nom, onNom }: Props) {
  const [emprunts, setEmprunts] = useState<Emprunt[] | null>(null)
  const [erreur, setErreur] = useState<string | null>(null)

  const charger = (n: string) => {
    if (!n.trim()) {
      setEmprunts(null)
      return
    }
    setErreur(null)
    api.emprunts(n.trim()).then(setEmprunts).catch((e: Error) => setErreur(e.message))
  }

  useEffect(() => {
    const t = setTimeout(() => charger(nom), 300)
    return () => clearTimeout(t)
  }, [nom])

  const rendre = async (e: Emprunt) => {
    try {
      await api.rendre(e.id)
      toast.success(`« ${e.titreLivre} » rendu`)
      charger(nom)
    } catch (err) {
      toast.error((err as Error).message)
    }
  }

  return (
    <section className="max-w-4xl">
      <div className="mb-8 grid max-w-sm gap-2">
        <Label htmlFor="emprunteur">Votre nom</Label>
        <Input id="emprunteur" value={nom} onChange={(e) => onNom(e.target.value)} className="h-11 bg-card text-base" />
      </div>

      {!nom.trim() && <p className="text-muted-foreground">Saisissez votre nom pour voir vos emprunts.</p>}
      {erreur && <p role="alert" className="text-destructive">Impossible de charger vos emprunts ({erreur}).</p>}
      {emprunts && emprunts.length === 0 && (
        <p className="text-muted-foreground">Aucun emprunt à ce nom. Choisissez un livre dans le catalogue.</p>
      )}

      {emprunts && emprunts.length > 0 && (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Livre</TableHead>
              <TableHead>Emprunté le</TableHead>
              <TableHead>À rendre le</TableHead>
              <TableHead>Statut</TableHead>
              <TableHead className="sr-only">Action</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {emprunts.map((e) => (
              <TableRow key={e.id}>
                <TableCell className="font-heading font-semibold">{e.titreLivre}</TableCell>
                <TableCell>{dateFr(e.dateEmprunt)}</TableCell>
                <TableCell className={e.statut === 'EN_RETARD' ? 'font-bold text-retard' : ''}>
                  {dateFr(e.dateRetourPrevue)}
                </TableCell>
                <TableCell>
                  <Badge variant={e.statut === 'EN_RETARD' ? 'destructive' : e.statut === 'RENDU' ? 'outline' : 'secondary'}>
                    {LIBELLES[e.statut]}
                  </Badge>
                </TableCell>
                <TableCell className="text-right">
                  {e.statut !== 'RENDU' && (
                    <Button size="sm" variant="outline" onClick={() => rendre(e)}>
                      Rendre
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </section>
  )
}
